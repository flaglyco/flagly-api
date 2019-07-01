package co.flagly.api.views

import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

final case class LoginAccount(email: String, password: String)

object LoginAccount {
  implicit val loginAccountReads: Reads[LoginAccount] =
    Reads[LoginAccount] {
      case json: JsObject =>
        val maybeLoginAccount =
          for {
            email    <- (json \ "email").asOpt[String]
            password <- (json \ "password").asOpt[String]
          } yield {
            LoginAccount(email, password)
          }

        maybeLoginAccount match {
          case None       => JsError(s"$json is not a valid LoginAccount!")
          case Some(flag) => JsSuccess(flag)
        }

      case json =>
        JsError(s"$json is not a valid LoginAccount!")
    }
}
