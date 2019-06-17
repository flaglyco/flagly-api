package co.flagly.api

import java.time.ZonedDateTime
import java.util.UUID

import co.flagly.core._
import play.api.libs.json._

package object models {
  implicit val dataTypeReads: Reads[DataType] =
    Reads[DataType] {
      case JsString(name) =>
        DataType.byName(name) match {
          case None           => JsError(s"Cannot read $name as DataType, it is invalid!")
          case Some(dataType) => JsSuccess(dataType)
        }

      case json => JsError(s"Cannot read $json as DataType, it is not a Json string!")
    }

  implicit val dataTypeWrites: Writes[DataType] = Writes[DataType](dataType => JsString(dataType.name))

  implicit val flagReads: Reads[Flag] =
    Reads[Flag] {
      case JsObject(jsObject) =>
        jsObject.get("dataType").flatMap(_.asOpt[DataType]) match {
          case None =>
            JsError(s"Cannot read $jsObject as Flag, it does not have a valid 'dataType'!")

          case Some(dataType) =>
            val maybeResult: Option[JsResult[Flag]] =
              for {
                id          <- jsObject.get("id").flatMap(_.asOpt[UUID])
                name        <- jsObject.get("name").flatMap(_.asOpt[String])
                description <- jsObject.get("description").flatMap(_.asOpt[String])
                createdAt   <- jsObject.get("createdAt").flatMap(_.asOpt[ZonedDateTime])
                updatedAt   <- jsObject.get("updatedAt").flatMap(_.asOpt[ZonedDateTime])
              } yield {
                val result: JsResult[Flag] =
                  dataType match {
                    case DataType.Boolean =>
                      jsObject.get("value").flatMap(_.asOpt[Boolean]) match {
                        case None        => JsError(s"Cannot read $jsObject as Flag, 'dataType' and 'value' does not match!")
                        case Some(value) => JsSuccess(BooleanFlag(id, name, description, value, createdAt, updatedAt))
                      }

                    case DataType.Number =>
                      jsObject.get("value").flatMap(_.asOpt[BigDecimal]) match {
                        case None        => JsError(s"Cannot read $jsObject as Flag, 'dataType' and 'value' does not match!")
                        case Some(value) => JsSuccess(NumberFlag(id, name, description, value, createdAt, updatedAt))
                      }

                    case DataType.Text =>
                      jsObject.get("value").flatMap(_.asOpt[String]) match {
                        case None        => JsError(s"Cannot read $jsObject as Flag, 'dataType' and 'value' does not match!")
                        case Some(value) => JsSuccess(TextFlag(id, name, description, value, createdAt, updatedAt))
                      }
                  }

                result
              }

            maybeResult.fold[JsResult[Flag]](JsError(s"Cannot read $jsObject as Flag, it is an invalid Json!"))(identity)
        }

      case json =>
        JsError(s"Cannot read $json as Flag, it is not a Json object!")
    }

  implicit val flagWrites: Writes[Flag] =
    Writes[Flag] { flag: Flag =>
      val value = flag match {
        case bf: BooleanFlag => JsBoolean(bf.value)
        case nf: NumberFlag  => JsNumber(nf.value)
        case tf: TextFlag    => JsString(tf.value)
      }

      Json.obj(
        "id"          -> flag.id,
        "name"        -> flag.name,
        "description" -> flag.description,
        "value"       -> value,
        "createdAt"   -> flag.createdAt,
        "updatedAt"   -> flag.updatedAt
      )
    }
}
