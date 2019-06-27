package co.flagly.api.views

import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

final case class CreateFlag(name: String,
                            description: Option[String],
                            value: Boolean)

object CreateFlag {
  implicit val createFlagReads: Reads[CreateFlag] =
    Reads[CreateFlag] {
      case json: JsObject =>
        val maybeCreateFlag =
          for {
            name        <- (json \ "name").asOpt[String]
            description  = (json \ "description").asOpt[String]
            value       <- (json \ "value").asOpt[Boolean]
          } yield {
            CreateFlag(name, description, value)
          }

        maybeCreateFlag match {
          case None       => JsError(s"$json is not a valid CreateFlag!")
          case Some(flag) => JsSuccess(flag)
        }

      case json =>
        JsError(s"$json is not a valid CreateFlag!")
    }
}
