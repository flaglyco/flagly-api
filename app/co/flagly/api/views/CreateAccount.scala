package co.flagly.api.views

import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

final case class CreateAccount(name: String,
                               email: String,
                               password: String)

object CreateAccount {
  implicit val createAccountReads: Reads[CreateAccount] =
    Reads[CreateAccount] {
      case json: JsObject =>
        val maybeCreateAccount =
          for {
            name     <- (json \ "name").asOpt[String]
            email    <- (json \ "email").asOpt[String]
            password <- (json \ "password").asOpt[String]
          } yield {
            CreateAccount(name, email, password)
          }

        maybeCreateAccount match {
          case None       => JsError(s"$json is not a valid CreateAccount!")
          case Some(flag) => JsSuccess(flag)
        }

      case json =>
        JsError(s"$json is not a valid CreateAccount!")
    }
}
