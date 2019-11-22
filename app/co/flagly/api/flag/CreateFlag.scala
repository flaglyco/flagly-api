package co.flagly.api.flag

import play.api.libs.json.{Format, Json}

final case class CreateFlag(name: String,
                            description: Option[String],
                            value: Boolean)

object CreateFlag {
  implicit val createFlagFormat: Format[CreateFlag] = Json.format[CreateFlag]
}
