package co.flagly.api.utilities

import play.api.libs.json.{JsObject, JsString}

object JsonUtils {
  def maskFields(json: JsObject, fields: String*): JsObject =
    fields.foldLeft(json) {
      case (j, field) =>
        j + (field -> JsString("HIDDEN"))
    }
}
