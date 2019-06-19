package co.flagly.api.models

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

import play.api.libs.json._

import scala.util.Try

trait JsonExtensions {
  private val zonedDateTimeFormatter: DateTimeFormatter =
    new DateTimeFormatterBuilder()
      .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      .optionalStart
      .appendOffsetId
      .toFormatter()

  implicit val zonedDateTimeReads: Reads[ZonedDateTime] =
    Reads[ZonedDateTime] {
      case JsString(s) =>
        Try(ZonedDateTime.parse(s, zonedDateTimeFormatter)).toOption match {
          case None      => JsError(s"$s is not a valid ZonedDateTime!")
          case Some(zdt) => JsSuccess(zdt)
        }

      case json =>
        JsError(s"$json is not a valid ZonedDateTime!")
    }

  implicit val zonedDateTimeWrites: Writes[ZonedDateTime] =
    Writes[ZonedDateTime] { zdt =>
      JsString(zdt.format(zonedDateTimeFormatter))
    }
}
