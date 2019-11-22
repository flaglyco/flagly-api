package co.flagly.api.common.base

import cats.effect.IO
import co.flagly.api.account.AccountCtx
import co.flagly.api.common.Errors
import co.flagly.api.durum._
import co.flagly.api.session.Session
import dev.akif.e.E
import dev.akif.e.playjson.eWrites
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames, Writeable}
import play.api.libs.json._
import play.api.mvc.Results.Status
import play.api.mvc._

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.Try

abstract class PlayDürüm[AUTH, CTX[BODY] <: Ctx[Request[AnyContent], BODY, AUTH]](cc: ControllerComponents) extends Dürüm[Request[AnyContent], Result, AUTH, CTX] {
  val logger: Logger

  def playAction(action: Request[AnyContent] => IO[Result]): Action[AnyContent] =
    cc.actionBuilder.async { request: Request[AnyContent] =>
      action(request).unsafeToFuture()
    }

  override def getHeadersOfRequest(request: Request[AnyContent]): Map[String, String] =
    request.headers.headers.toMap

  override def getMethodOfRequest(request: Request[AnyContent]): String =
    request.method

  override def getURIOfRequest(request: Request[AnyContent]): String =
    request.uri

  override def getStatusOfResponse(result: Result): Int =
    result.header.status

  @tailrec
  final override def buildFailedResponse(throwable: Throwable): IO[Result] =
    throwable match {
      case e: E => buildResult(e)
      case t    => buildFailedResponse(Errors.from(t))
    }

  @tailrec
  final override def buildFailedResponseAsString(throwable: Throwable): IO[String] =
    throwable match {
      case e: E => IO.pure(e.toString)
      case t    => buildFailedResponseAsString(Errors.from(t))
    }

  override def responseWithHeader(result: Result, header: (String, String)): Result =
    result.withHeaders(header)

  override def getHeadersOfResponse(result: Result): Map[String, String] =
    result.header.headers

  override def logRequest(log: RequestLog, failed: Boolean): Unit =
    if (failed) {
      logger.error(log.toLogString(isIncoming = true))
    } else {
      logger.info(log.toLogString(isIncoming = true))
    }

  override def logResponse(log: ResponseLog, failed: Boolean): Unit =
    if (failed) {
      logger.error(log.toLogString(isIncoming = false))
    } else {
      logger.info(log.toLogString(isIncoming = false))
    }

  def buildResult(status: Int): IO[Result] =
    IO.pure {
      Status(status)
    }

  def buildResult[O: Writeable](out: O, status: Int): IO[Result] =
    IO.pure {
      Status(status)(out)
    }

  def buildResult(e: E): IO[Result] =
    IO.pure {
      Status(e.code)(Json.toJson(e)).as(ContentTypes.JSON)
    }

  protected def getBearerToken(request: Request[_]): IO[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) match {
      case None =>
        IO.raiseError(Errors.unauthorized("Bearer token is missing!"))

      case Some(h) if !h.startsWith("Bearer ") =>
        IO.raiseError(Errors.unauthorized("Token is not a bearer token!").data("token", h))

      case Some(header) =>
        IO.pure(header.drop(7))
    }

  object implicits {
    implicit val requestBuilderUnit: RequestBuilder[Request[AnyContent], Unit] =
      new RequestBuilder[Request[AnyContent], Unit] {
        override def build(request: Request[AnyContent]): IO[Unit] = IO.unit

        override def log(request: Request[AnyContent]): IO[String] = IO.pure("")
      }

    implicit def requestBuilderJson[A: Reads : Writes]: RequestBuilder[Request[AnyContent], A] =
      new RequestBuilder[Request[AnyContent], A] {
        override def build(request: Request[AnyContent]): IO[A] =
          getAnyContentAsStringOrJson(request.body) match {
            case Left(body) =>
              IO.raiseError(Errors.badRequest.message("Request body is invalid!").data("body", body))

            case Right(json) =>
              json.validate[A] match {
                case JsError(errors) =>
                  IO.raiseError {
                    errors.foldLeft(Errors.badRequest.message("Request body is invalid!")) {
                      case (e, (path, es)) =>
                        e.data(path.path.map(_.toJsonString).mkString, es.flatMap(_.messages).mkString(", "))
                    }
                  }

                case JsSuccess(in, _) =>
                  IO.pure(in)
              }
          }

        override def log(request: Request[AnyContent]): IO[String] =
          getAnyContentAsStringOrJson(request.body) match {
            case Left(body) =>
              IO.pure(body)

            case Right(json) =>
              json.validate[A] match {
                case JsError(_) =>
                  IO.pure(json.toString)

                case JsSuccess(a, _) =>
                  IO.pure(Json.toJson(a).toString)
              }
          }
      }

    implicit def responseBuilderJson[A: Writes]: ResponseBuilder[A, Result] =
      new ResponseBuilder[A, Result] {
        override def build(status: Int, a: A): IO[Result] =
          IO.pure {
            Status(status)(Json.toJson(a)).as(ContentTypes.JSON)
          }

        override def log(a: A): IO[String] =
          IO.pure {
            Json.toJson(a).toString()
          }
      }

    implicit def responseBuilderJsonWithSession[A: Writes : ClassTag]: ResponseBuilder[(A, Session), Result] =
      new ResponseBuilder[(A, Session), Result] {
        override def build(status: Int, tuple: (A, Session)): IO[Result] =
          tuple match {
            case (a: A, session: Session) =>
              responseBuilderJson[A].build(status, a).map { result =>
                responseWithHeader(result, AccountCtx.sessionTokenHeaderName -> session.token)
              }
          }

        override def log(tuple: (A, Session)): IO[String] =
          tuple match {
            case (a: A, _: Session) =>
              responseBuilderJson[A].log(a)
          }
      }

    @tailrec
    private final def getAnyContentAsStringOrJson(body: AnyContent): Either[String, JsValue] =
      body match {
        case AnyContentAsEmpty =>
          Left("")

        case AnyContentAsText(txt) =>
          Left(txt)

        case AnyContentAsFormUrlEncoded(form) =>
          val formAsText = form.flatMap {
            case (k, vs) =>
              vs.map(v => if (Option(v).map(_.trim).getOrElse("").isEmpty) k else s"$k=$v")
          }.mkString("&")
          Left(formAsText)

        case AnyContentAsRaw(raw) =>
          val rawAsText = raw.asBytes().mkString
          Try(Json.parse(rawAsText)).toEither.fold(_ => Left(rawAsText), j => Right(j))

        case AnyContentAsXml(xml) =>
          Left(xml.toString())

        case AnyContentAsMultipartFormData(mfd) =>
          getAnyContentAsStringOrJson(AnyContentAsFormUrlEncoded(mfd.asFormUrlEncoded))

        case AnyContentAsJson(json) =>
          Right(json)

        case b =>
          Left(b.toString)
      }
  }
}
