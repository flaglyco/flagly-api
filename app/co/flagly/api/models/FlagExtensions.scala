package co.flagly.api.models

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, Success}
import co.flagly.data.Flag
import play.api.libs.json._

object FlagExtensions extends JsonExtensions {
  implicit val flagReads: Reads[Flag] =
    Reads[Flag] {
      case json: JsObject =>
        val maybeFlag = for {
          id          <- (json \ "id").asOpt[UUID]
          name        <- (json \ "name").asOpt[String]
          description <- (json \ "description").asOpt[String]
          value       <- (json \ "value").asOpt[Boolean]
          createdAt   <- (json \ "createdAt").asOpt[ZonedDateTime]
          updatedAt   <- (json \ "updatedAt").asOpt[ZonedDateTime]
        } yield {
          Flag(id, name, description, value, createdAt, updatedAt)
        }

        maybeFlag match {
          case None       => JsError(s"$json is not a valid Flag!")
          case Some(flag) => JsSuccess(flag)
        }

      case json =>
        JsError(s"$json is not a valid Flag!")
    }

  implicit val flagWrites: Writes[Flag] =
    Writes[Flag] { flag =>
      Json.obj(
        "id"          -> flag.id,
        "name"        -> flag.name,
        "description" -> flag.description,
        "value"       -> flag.value,
        "createdAt"   -> flag.createdAt,
        "updatedAt"   -> flag.updatedAt
      )
    }

  implicit val flagRowParser: RowParser[Flag] =
    RowParser[Flag] { row =>
      val id          = row[UUID]("id")
      val name        = row[String]("name")
      val description = row[String]("description")
      val value       = row[Boolean]("value")
      val createdAt   = row[ZonedDateTime]("created_at")
      val updatedAt   = row[ZonedDateTime]("updated_at")

      Success(Flag(id, name, description, value, createdAt, updatedAt))
    }
}
