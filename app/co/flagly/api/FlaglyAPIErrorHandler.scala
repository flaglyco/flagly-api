package co.flagly.api

import dev.akif.e.E
import play.api.http.{ContentTypes, HttpErrorHandler}
import play.api.mvc.Results.{InternalServerError, Status}
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

class FlaglyAPIErrorHandler extends HttpErrorHandler {
  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String): Future[Result] =
    Future.successful(Status(statusCode)(message))

  override def onServerError(request: RequestHeader,
                             exception: Throwable): Future[Result] =
    exception match {
      case e: E =>
        // TODO: Delete `e.data` here on production since it may contain sensitive data which is helpful for debugging on non-production environments
        Future.successful(Status(e.code)(e.toString).as(ContentTypes.JSON))

      case t =>
        Future.successful(InternalServerError(t.getMessage))
    }
}
