package co.flagly.api.application

import play.api.libs.json.{Format, Json}

final case class CreateApplication(name: String)

object CreateApplication {
  implicit val createApplicationFormat: Format[CreateApplication] = Json.format[CreateApplication]
}
