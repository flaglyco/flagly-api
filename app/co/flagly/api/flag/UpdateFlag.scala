package co.flagly.api.flag

import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

final case class UpdateFlag(name: Option[String],
                            description: Option[String],
                            value: Option[Boolean])

object UpdateFlag {
  implicit val updateFlagReads: Reads[UpdateFlag] =
    Reads[UpdateFlag] {
      case json: JsObject =>
        val name        = (json \ "name").asOpt[String]
        val description = (json \ "description").asOpt[String]
        val value       = (json \ "value").asOpt[Boolean]

        JsSuccess(UpdateFlag(name, description, value))

      case json =>
        JsError(s"$json is not a valid UpdateFlag!")
    }
}
