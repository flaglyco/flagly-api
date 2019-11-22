package co.flagly.api.durum

import cats.effect.IO

trait Dürüm[REQ, RES, AUTH, CTX[BODY] <: Ctx[REQ, BODY, AUTH]] {
  def getHeadersOfRequest(request: REQ): Map[String, String]

  def getMethodOfRequest(request: REQ): String

  def getURIOfRequest(request: REQ): String

  def buildAuth(request: REQ): IO[AUTH]

  def buildContext[IN](id: String,
                       request: REQ,
                       headers: Map[String, String],
                       in: IN,
                       auth: AUTH,
                       time: Long): CTX[IN]

  def getStatusOfResponse(response: RES): Int

  def buildFailedResponse(throwable: Throwable): IO[RES]

  def buildFailedResponseAsString(throwable: Throwable): IO[String]

  def responseWithHeader(response: RES, header: (String, String)): RES

  def getHeadersOfResponse(response: RES): Map[String, String]

  def logRequest(log: RequestLog, failed: Boolean): Unit

  def logResponse(log: ResponseLog, failed: Boolean): Unit

  def basicAction(request: REQ)(action: CTX[Unit] => IO[RES]): IO[RES] =
    actionImplementation[Unit, RES](
      request,
      _   => IO.unit,
      _   => IO.pure(""),
      ctx => action(ctx),
      res => IO.pure(res),
      _   => IO.pure("")
    )

  def actionWithInput[IN](request: REQ)(action: CTX[IN] => IO[RES])(implicit requestBuilder: RequestBuilder[REQ, IN]): IO[RES] =
    actionImplementation[IN, RES](
      request,
      requestBuilder.build,
      requestBuilder.log,
      ctx => action(ctx),
      res => IO.pure(res),
      _   => IO.pure("")
    )

  def actionWithOutput[OUT](request: REQ, responseStatus: Int = 200)(action: CTX[Unit] => IO[OUT])(implicit responseBuilder: ResponseBuilder[OUT, RES]): IO[RES] =
    actionImplementation[Unit, OUT](
      request,
      _   => IO.unit,
      _   => IO.pure(""),
      ctx => action(ctx),
      out => responseBuilder.build(responseStatus, out),
      responseBuilder.log
    )

  def actionWithInputAndOutput[IN, OUT](request: REQ, responseStatus: Int = 200)(action: CTX[IN] => IO[OUT])(implicit requestBuilder: RequestBuilder[REQ, IN],
                                                                                                                      responseBuilder: ResponseBuilder[OUT, RES]): IO[RES] =
    actionImplementation[IN, OUT](
      request,
      requestBuilder.build,
      requestBuilder.log,
      ctx => action(ctx),
      out => responseBuilder.build(responseStatus, out),
      responseBuilder.log
    )

  protected def actionImplementation[IN, OUT](request: REQ,
                                              getRequestBody: REQ => IO[IN],
                                              getRequestBodyAsString: REQ => IO[String],
                                              action: CTX[IN] => IO[OUT],
                                              buildResponse: OUT => IO[RES],
                                              getResponseBodyAsString: OUT => IO[String]): IO[RES] = {
    val requestHeaders = getHeadersOfRequest(request)
    val requestId             = Ctx.getOrCreateId(requestHeaders)
    val requestTime           = System.currentTimeMillis
    val requestMethod         = getMethodOfRequest(request)
    val requestURI            = getURIOfRequest(request)

    val requestBodyProcessorErrorHandler: Throwable => IO[(Either[Throwable, OUT], RES)] = { bodyProcessingError: Throwable =>
      for {
        requestBodyAsString <- getRequestBodyAsString(request)
        failedResponse      <- buildFailedResponse(bodyProcessingError)
      } yield {
        val log = RequestLog(requestMethod, requestURI, requestId, requestTime, requestHeaders, requestBodyAsString)
        logRequest(log, failed = true)
        Left(bodyProcessingError) -> failedResponse
      }
    }

    val requestBodyHandler: IN => IO[(Either[Throwable, OUT], RES)] = { in: IN =>
        for {
          requestBodyAsString <- getRequestBodyAsString(request)
          log                  = RequestLog(requestMethod, requestURI, requestId, requestTime, requestHeaders, requestBodyAsString)
          _                    = logRequest(log, failed = false)
          auth                <- buildAuth(request)
          ctx                  = buildContext[IN](requestId, request, requestHeaders, in, auth, requestTime)
          out                 <- action(ctx)
          response            <- buildResponse(out)
        } yield {
          Right(out) -> response
        }
      }

    val requestIO: IO[(Either[Throwable, OUT], RES)] =
      getRequestBody(request).redeemWith(
        bodyProcessingError => requestBodyProcessorErrorHandler(bodyProcessingError),
        in                  => requestBodyHandler(in)
      )

    val responseIO: IO[(Either[Throwable, OUT], RES)] =
      requestIO.handleErrorWith { requestProcessingError: Throwable =>
        buildFailedResponse(requestProcessingError).map { failedResponse =>
          Left(requestProcessingError) -> failedResponse
        }
      }

    val responseAsStringIO: Either[Throwable, OUT] => IO[(String, Boolean)] = _.fold(
      error => buildFailedResponseAsString(error).map(s => s -> true),
      out   => getResponseBodyAsString(out).map(s => s -> false)
    )

    for {
      resultDataAndResponse          <- responseIO
      (errorOrOut, response)          = resultDataAndResponse
      finalResponse                   = responseWithHeader(response, Ctx.requestIdHeaderName -> requestId)
      responseHeaders                 = getHeadersOfResponse(finalResponse)
      responseBodyAndFailed          <- responseAsStringIO(errorOrOut)
      (responseBodyAsString, failed)  = responseBodyAndFailed
      responseStatus                  = getStatusOfResponse(finalResponse)
    } yield {
      val log = ResponseLog(responseStatus, requestMethod, requestURI, requestId, requestTime, responseHeaders, responseBodyAsString)
      logResponse(log, failed)
      finalResponse
    }
  }
}
