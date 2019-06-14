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
import com.github.makiftutuncu.switchboard.{Flag, Switchboard}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class FlagController(val switchboard: Switchboard)(implicit ec: ExecutionContext) extends FutureDirectives with FailFastCirceSupport {
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
      setFlag ~ getFlag
    }

  private def setFlag: Route = {
    path("flags") {
      post {
        entity(as[Flag[Boolean]]) { flag: Flag[Boolean] =>
          onSuccess(switchboard.dataSource.setFlag(flag)) { flag =>
            completeAsJson(flag.encode)
          }
        }
      }
    }
  }

  private def getFlag: Route = {
    path("flags" / Segment) { flagId =>
      get {
        onSuccess(switchboard.dataSource.getFlag[Boolean](UUID.fromString(flagId))) {
          case None       => complete(HttpResponse(StatusCodes.NotFound, entity = s"Flag ${UUID.fromString(flagId)} is not found!"))
          case Some(flag) => completeAsJson(flag.encode)
        }
      }
    }
  }

  private def completeAsJson(string: String): Route =
    complete {
      HttpResponse(entity = Strict(ContentTypes.`application/json`, ByteString(string)))
    }
}
