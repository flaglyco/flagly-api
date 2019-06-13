package com.github.makiftutuncu.switchboard

import java.util.UUID

import play.api.libs.json._

import scala.util.Try

package object playjson {
  implicit def playJsonDecoder[A](implicit reads: Reads[A]): Decoder[A] = { input: String =>
    for {
      json <- Try(Json.parse(input)).toOption
      a    <- json.asOpt[A]
    } yield {
      a
    }
  }

  implicit def playJsonEncoder[A](implicit writes: Writes[A]): Encoder[A] = { input: A =>
    writes.writes(input).toString()
  }

  implicit def flagReads[A](implicit reads: Reads[A]): Reads[Flag[A]] =
    Reads[Flag[A]] {
      case json: JsObject =>
        val maybeFlag = for {
          id           <- (json \ "id").asOpt[UUID]
          name         <- (json \ "name").asOpt[String]
          description  <- (json \ "description").asOpt[String]
          value        <- (json \ "value").asOpt[A]
          defaultValue <- (json \ "defaultValue").asOpt[A]
        } yield {
          Flag[A](id, name, description, value, defaultValue)
        }

        maybeFlag.fold[JsResult[Flag[A]]](JsError(s"$json is not a valid Flag"))(flag => JsSuccess(flag))

      case json =>
        JsError(s"$json is not an object")
    }

  implicit def flagWrites[A](implicit writes: Writes[A]): Writes[Flag[A]] =
    Writes[Flag[A]] { flag: Flag[A] =>
      Json.obj(
        "id"           -> flag.id,
        "name"         -> flag.name,
        "description"  -> flag.description,
        "value"        -> flag.value,
        "defaultValue" -> flag.defaultValue
      )
    }
}
