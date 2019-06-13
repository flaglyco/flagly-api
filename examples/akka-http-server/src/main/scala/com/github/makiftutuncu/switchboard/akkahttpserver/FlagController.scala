package com.github.makiftutuncu.switchboard.akkahttpserver

import java.util.UUID

import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model.{ContentTypes, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, handleExceptions, path, _}
import akka.http.scaladsl.server.directives.FutureDirectives
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.util.ByteString
import com.github.makiftutuncu.switchboard.Encoder.syntax
import com.github.makiftutuncu.switchboard.circe._
import com.github.makiftutuncu.switchboard._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class FlagController extends FutureDirectives with FailFastCirceSupport {
  val http: Http = new Http {}

  val dataSource: DataSource = new DataSource {
    override def getFlag[A](id: UUID)(implicit decoder: Decoder[Flag[A]], ec: ExecutionContext): Future[Option[Flag[A]]] = Future.successful(None)
    override def setFlag[A](flag: Flag[A])(implicit encoder: Encoder[Flag[A]], ec: ExecutionContext): Future[Flag[A]]    = Future.successful(flag)
  }

  val switchboard: Switchboard = new Switchboard(http, dataSource)

  val errorHandler: ExceptionHandler =
    ExceptionHandler {
      case NonFatal(t) =>
        val message = "Failed to handle request!"
        println(message)
        t.printStackTrace()
        complete(HttpResponse(StatusCodes.InternalServerError, entity = s"$message ${t.getMessage}"))
    }

  val route: Route =
    handleExceptions(errorHandler) {
      getAllFlags ~ getFlag
    }

  private def getAllFlags: Route = {
    path("flags") {
      get {
        val flags = s"[${Flags.maintenanceMode.encode},${Flags.httpTimeout.encode}]"
        completeAsJson(flags)
      }
    }
  }

  private def getFlag: Route = {
    path("flags" / Segment) { flagId =>
      get {
        val id = UUID.fromString(flagId)

        if (id == Flags.maintenanceMode.id) {
          completeAsJson(Flags.maintenanceMode.encode)
        } else if (id == Flags.httpTimeout.id) {
          completeAsJson(Flags.httpTimeout.encode)
        } else {
          complete(HttpResponse(StatusCodes.NotFound, entity = s"Flag $id is not found!"))
        }
      }
    }
  }

  private def completeAsJson(string: String): Route =
    complete {
      HttpResponse(entity = Strict(ContentTypes.`application/json`, ByteString(string)))
    }
}
