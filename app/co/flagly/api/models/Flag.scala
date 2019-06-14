package co.flagly.api.models

import java.util.UUID

import play.api.libs.json.{Json, Writes}

final case class Flag(id: UUID,
                      name: String,
                      description: String,
                      value: String)

object Flag {
  implicit val flagWrites: Writes[Flag] = Json.writes[Flag]

  def apply(name: String,
            description: String,
            value: String): Flag =
    new Flag(
      UUID.randomUUID,
      name,
      description,
      value
    )
}
