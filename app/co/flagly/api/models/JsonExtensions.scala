package co.flagly.api.models

import java.time.ZonedDateTime

import co.flagly.utils.ZDT
import play.api.libs.json._

import scala.util.Try

trait JsonExtensions {
  implicit val zonedDateTimeReads: Reads[ZonedDateTime] =
    Reads[ZonedDateTime] {
      case JsString(s) =>
        Try(ZDT.fromString(s)).toOption match {
          case None      => JsError(s"$s is not a valid ZonedDateTime!")
          case Some(zdt) => JsSuccess(zdt)
        }

      case json =>
        JsError(s"$json is not a valid ZonedDateTime!")
    }

  implicit val zonedDateTimeWrites: Writes[ZonedDateTime] =
    Writes[ZonedDateTime] { zdt =>
      JsString(ZDT.toString(zdt))
    }
}
