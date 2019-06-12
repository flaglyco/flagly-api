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
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.syntax._

import scala.util.control.NonFatal

class FlagController extends FutureDirectives with FailFastCirceSupport {
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
        val flags = List(Flags.maintenanceMode.encode, Flags.httpTimeout.encode)
        completeWithJson(flags.asJson)
      }
    }
  }

  private def getFlag: Route = {
    path("flags" / Segment) { flagId =>
      get {
        val id = UUID.fromString(flagId)

        if (id == Flags.maintenanceMode.id) {
          completeWithJson(Flags.maintenanceMode.encode)
        } else if (id == Flags.httpTimeout.id) {
          completeWithJson(Flags.httpTimeout.encode)
        } else {
          complete(HttpResponse(StatusCodes.NotFound, entity = s"Flag $id is not found!"))
        }
      }
    }
  }

  private def completeWithJson(json: Json): Route =
    complete {
      HttpResponse(entity = Strict(ContentTypes.`application/json`, ByteString(json.noSpaces)))
    }
}
