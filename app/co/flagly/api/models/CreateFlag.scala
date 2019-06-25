package co.flagly.api.models

import java.util.UUID

import co.flagly.core.Flag
import play.api.libs.json.{Json, Reads}

final case class CreateFlag(name: String,
                            description: Option[String],
                            value: Boolean) { self =>
  def toFlag(applicationId: UUID): Flag = Flag.of(applicationId, name, description.getOrElse(""), value)
}

object CreateFlag {
  implicit val createFlagReads: Reads[CreateFlag] = Json.reads[CreateFlag]
}
