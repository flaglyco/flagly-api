package co.flagly.api.application

import play.api.libs.json.{Format, Json}

final case class UpdateApplication(name: String)

object UpdateApplication {
  implicit val updateApplicationFormat: Format[UpdateApplication] = Json.format[UpdateApplication]
}
