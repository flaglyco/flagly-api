package co.flagly.api

import co.flagly.api.common.Errors
import dev.akif.e.E
import play.api.http.HttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.{Logger, Logging}

import scala.concurrent.Future

class ErrorHandler(logger: Logger, handleError: (RequestHeader, E) => Future[Result]) extends HttpErrorHandler with Logging {
  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String): Future[Result] =
    handleError(request, Errors.unexpected.message(message).code(statusCode))

  override def onServerError(request: RequestHeader,
                             exception: Throwable): Future[Result] =
    exception match {
      case e: E => handleError(request, e)
      case t    => handleError(request, Errors.unexpected.cause(t))
    }
}
