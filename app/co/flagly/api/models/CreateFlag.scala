package co.flagly.api.models

import play.api.libs.json.{Json, Reads}

final case class CreateFlag(name: String,
                            description: Option[String],
                            value: String) { self =>
  def toFlag: Flag = Flag(self.name, self.description.getOrElse(""), self.value)
}

object CreateFlag {
  implicit val createFlagReads: Reads[CreateFlag] = Json.reads[CreateFlag]
}
