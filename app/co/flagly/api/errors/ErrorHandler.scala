package co.flagly.api.errors

import play.api.http.HttpErrorHandler
import play.api.mvc.Results.{BadRequest, InternalServerError}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = Future.successful(BadRequest(message))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = Future.successful(InternalServerError(exception.getMessage))
}
