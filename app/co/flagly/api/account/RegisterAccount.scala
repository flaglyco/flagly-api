package co.flagly.api.account

import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

final case class RegisterAccount(name: String,
                                 email: String,
                                 password: String)

object RegisterAccount {
  implicit val registerAccountReads: Reads[RegisterAccount] =
    Reads[RegisterAccount] {
      case json: JsObject =>
        val maybeCreateAccount =
          for {
            name     <- (json \ "name").asOpt[String]
            email    <- (json \ "email").asOpt[String]
            password <- (json \ "password").asOpt[String]
          } yield {
            RegisterAccount(name, email, password)
          }

        maybeCreateAccount match {
          case None       => JsError(s"$json is not a valid RegisterAccount!")
          case Some(flag) => JsSuccess(flag)
        }

      case json =>
        JsError(s"$json is not a valid RegisterAccount!")
    }
}
