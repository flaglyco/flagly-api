package co.flagly.api.account

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, Success}
import co.flagly.api.utilities.PasswordUtils
import co.flagly.utils.ZDT
import play.api.libs.json.{JsObject, Json, Writes}

final case class Account(id: UUID,
                         name: String,
                         email: String,
                         password: String,
                         salt: String,
                         createdAt: ZonedDateTime,
                         updatedAt: ZonedDateTime)

object Account {
  implicit val accountWrites: Writes[Account] =
    Json.writes[Account].transform { json: JsObject =>
      json - "password" - "salt"
    }

  implicit val accountRowParser: RowParser[Account] =
    RowParser[Account] { row =>
      val id        = row[UUID]("id")
      val name      = row[String]("name")
      val email     = row[String]("email")
      val password  = row[String]("password")
      val salt      = row[String]("salt")
      val createdAt = row[ZonedDateTime]("created_at")
      val updatedAt = row[ZonedDateTime]("updated_at")

      Success(Account(id, name, email, password, salt, createdAt, updatedAt))
    }

  def apply(registerAccount: RegisterAccount): Account = {
    val salt = PasswordUtils.generateSalt()

    Account(
      id        = UUID.randomUUID,
      name      = registerAccount.name,
      email     = registerAccount.email,
      password  = PasswordUtils.hash(registerAccount.password, salt),
      salt      = salt,
      createdAt = ZDT.now,
      updatedAt = ZDT.now
    )
  }
}
