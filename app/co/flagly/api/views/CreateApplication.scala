package co.flagly.api.views

import play.api.libs.json.{Json, Reads}

final case class CreateApplication(name: String)

object CreateApplication {
  implicit val createApplicationReads: Reads[CreateApplication] = Json.reads[CreateApplication]
}
