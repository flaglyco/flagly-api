package co.flagly.api.models

import co.flagly.core.Flag
import co.flagly.utils.ZDT
import play.api.libs.json.{Json, Reads}

final case class UpdateFlag(name: Option[String],
                            description: Option[String],
                            value: Option[Boolean]) { self =>
  def toUpdatedFlag(flag: Flag): Flag =
    Flag.of(
      flag.id,
      flag.applicationId,
      self.name.getOrElse(flag.name),
      self.description.getOrElse(flag.description),
      self.value.getOrElse(flag.value),
      flag.createdAt,
      ZDT.now
    )
}

object UpdateFlag {
  implicit val updateFlagReads: Reads[UpdateFlag] = Json.reads[UpdateFlag]
}
