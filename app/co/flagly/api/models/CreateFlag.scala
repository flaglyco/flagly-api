package co.flagly.api.models

import co.flagly.data.Flag
import play.api.libs.json.{Json, Reads}

final case class CreateFlag(name: String,
                            description: Option[String],
                            value: Boolean) { self =>
  def toFlag: Flag = Flag(name, description.getOrElse(""), value)
}

object CreateFlag {
  implicit val createFlagReads: Reads[CreateFlag] = Json.reads[CreateFlag]
}
