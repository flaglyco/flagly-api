package com.github.makiftutuncu.switchboard.akkahttpserver

import akka.http.scaladsl.model.HttpEntity.{ChunkStreamPart, Chunked}
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, get, handleExceptions, path, _}
import akka.http.scaladsl.server.directives.FutureDirectives
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.scaladsl.Source
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json

import scala.util.control.NonFatal

class FlagController extends FutureDirectives with FailFastCirceSupport {
  val errorHandler: ExceptionHandler =
    ExceptionHandler {
      case NonFatal(t) =>
        val message = "Failed to handle request!"
        println(message)
        t.printStackTrace()
        complete(s"$message ${t.getMessage}")
    }

  val route: Route =
    handleExceptions(errorHandler) {
      getAllFlags ~ getFlag
    }

  private def getAllFlags: Route = {
    path("flags") {
      get {
        completeWithJson(Json.arr())
      }
    }
  }

  private def getFlag: Route = {
    path("flags" / Segment) { flagId =>
      get {
        completeWithJson(Json.obj())
      }
    }
  }

  private def completeWithJson(json: Json): Route =
    complete {
      HttpResponse(entity = Chunked(ContentTypes.`application/json`, Source.single(json.noSpaces).map(ChunkStreamPart.apply)))
    }
}
