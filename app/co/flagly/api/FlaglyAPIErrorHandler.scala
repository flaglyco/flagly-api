package co.flagly.api

import co.flagly.core.FlaglyError
import play.api.http.{ContentTypes, HttpErrorHandler}
import play.api.mvc.Results.{BadRequest, InternalServerError, Status}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

class FlaglyAPIErrorHandler extends HttpErrorHandler {
  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String): Future[Result] =
    Future.successful(BadRequest(message))

  override def onServerError(request: RequestHeader,
                             exception: Throwable): Future[Result] =
    exception match {
      case flaglyError: FlaglyError =>
        Future.successful(Status(flaglyError.code)(flaglyError.toString).as(ContentTypes.JSON))

      case t =>
        Future.successful(InternalServerError(t.getMessage))
    }
}
