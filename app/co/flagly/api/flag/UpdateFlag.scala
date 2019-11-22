package co.flagly.api.flag

import play.api.libs.json.{Format, Json}

final case class UpdateFlag(name: Option[String],
                            description: Option[String],
                            value: Option[Boolean])

object UpdateFlag {
  implicit val updateFlagFormat: Format[UpdateFlag] = Json.format[UpdateFlag]
}
