package co.flagly.api.account

import co.flagly.api.utilities.JsonUtils
import play.api.libs.json.{JsObject, Json, Reads, Writes}

final case class LoginAccount(email: String, password: String)

object LoginAccount {
  implicit val loginAccountReads: Reads[LoginAccount] =
    Json.reads[LoginAccount]

  implicit val loginAccountWrites: Writes[LoginAccount] =
    Json.writes[LoginAccount].transform { json: JsObject =>
      JsonUtils.maskFields(json, "password")
    }
}
