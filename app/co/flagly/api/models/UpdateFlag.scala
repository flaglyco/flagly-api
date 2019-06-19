package co.flagly.api.models

import java.time.ZonedDateTime

import co.flagly.data.Flag
import play.api.libs.json.{Json, Reads}

final case class UpdateFlag(name: Option[String],
                            description: Option[String],
                            value: Option[Boolean]) { self =>
  def toUpdatedFlag(flag: Flag): Flag =
    flag.copy(
      name        = self.name.getOrElse(flag.name),
      description = self.description.getOrElse(flag.description),
      value       = self.value.getOrElse(flag.value),
      updatedAt   = ZonedDateTime.now
    )
}

object UpdateFlag {
  implicit val updateFlagReads: Reads[UpdateFlag] = Json.reads[UpdateFlag]
}
