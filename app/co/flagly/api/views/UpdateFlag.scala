package co.flagly.api.views

import play.api.libs.json.{Json, Reads}

final case class UpdateFlag(name: Option[String],
                            description: Option[String],
                            value: Option[Boolean])

object UpdateFlag {
  implicit val updateFlagReads: Reads[UpdateFlag] = Json.reads[UpdateFlag]
}
