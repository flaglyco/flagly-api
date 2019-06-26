package co.flagly.api.views

import play.api.libs.json.{Json, Reads}

final case class CreateAccount(name: String,
                               email: String,
                               password: String)

object CreateAccount {
  implicit val createAccountReads: Reads[CreateAccount] = Json.reads[CreateAccount]
}
