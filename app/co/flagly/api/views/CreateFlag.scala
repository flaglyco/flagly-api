package co.flagly.api.views

import play.api.libs.json.{Json, Reads}

final case class CreateFlag(name: String,
                            description: Option[String],
                            value: Boolean)

object CreateFlag {
  implicit val createFlagReads: Reads[CreateFlag] = Json.reads[CreateFlag]
}
