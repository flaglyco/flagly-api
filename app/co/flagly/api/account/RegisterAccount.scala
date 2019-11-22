package co.flagly.api.account

import co.flagly.api.utilities.JsonUtils
import play.api.libs.json.{JsObject, Json, Reads, Writes}

final case class RegisterAccount(name: String,
                                 email: String,
                                 password: String)

object RegisterAccount {
  implicit val registerAccountReads: Reads[RegisterAccount] =
    Json.reads[RegisterAccount]

  implicit val registerAccountWrites: Writes[RegisterAccount] =
    Json.writes[RegisterAccount].transform { json: JsObject =>
      JsonUtils.maskFields(json, "password")
    }
}
