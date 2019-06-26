package co.flagly.api.views

import play.api.libs.json.{Json, Reads}

final case class UpdateApplication(name: String)

object UpdateApplication {
  implicit val updateApplicationReads: Reads[UpdateApplication] = Json.reads[UpdateApplication]
}
