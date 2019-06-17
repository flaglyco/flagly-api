package co.flagly.api.models

import java.time.ZonedDateTime

import co.flagly.core.{BooleanFlag, Flag, NumberFlag, TextFlag}
import play.api.libs.json.{JsValue, Json, Reads}

final case class UpdateFlag(name: Option[String],
                            description: Option[String],
                            value: Option[JsValue]) { self =>
  def toUpdatedFlag(flag: Flag): Flag =
    flag match {
      case bFlag: BooleanFlag =>
        bFlag.copy(
          name        = self.name.getOrElse(bFlag.name),
          description = self.description.getOrElse(bFlag.description),
          value       = self.value.flatMap(_.asOpt[Boolean]).getOrElse(bFlag.value),
          updatedAt   = ZonedDateTime.now
        )

      case nFlag: NumberFlag =>
        nFlag.copy(
          name        = self.name.getOrElse(nFlag.name),
          description = self.description.getOrElse(nFlag.description),
          value       = self.value.flatMap(_.asOpt[BigDecimal]).getOrElse(nFlag.value),
          updatedAt   = ZonedDateTime.now
        )

      case tFlag: TextFlag =>
        tFlag.copy(
          name        = self.name.getOrElse(tFlag.name),
          description = self.description.getOrElse(tFlag.description),
          value       = self.value.flatMap(_.asOpt[String]).getOrElse(tFlag.value),
          updatedAt   = ZonedDateTime.now
        )
    }
}

object UpdateFlag {
  implicit val updateFlagReads: Reads[UpdateFlag] = Json.reads[UpdateFlag]
}
