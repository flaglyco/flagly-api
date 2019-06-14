package co.flagly.api.models

import play.api.libs.json.{Json, Reads}

final case class UpdateFlag(name: Option[String],
                            description: Option[String],
                            value: Option[String]) { self =>
  def toUpdatedFlag(flag: Flag): Flag = Flag(self.name.getOrElse(flag.name), self.description.getOrElse(flag.description), self.value.getOrElse(flag.value))
}

object UpdateFlag {
  implicit val updateFlagReads: Reads[UpdateFlag] = Json.reads[UpdateFlag]
}
