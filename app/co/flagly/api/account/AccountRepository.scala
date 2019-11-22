package co.flagly.api.account

import java.sql.Connection
import java.util.UUID

import anorm.SQL
import cats.effect.IO
import co.flagly.api.account.Account.accountRowParser
import co.flagly.api.common.Errors
import co.flagly.api.common.Errors.PSQL
import co.flagly.api.common.base.Repository

class AccountRepository extends Repository {
  def create(account: Account)(implicit connection: Connection): IO[Account] =
    ioHandlingErrors {
      SQL(
        """
          |INSERT INTO accounts(id, name, email, password, salt, created_at, updated_at)
          |VALUES({id}::uuid, {name}, {email}, {password}, {salt}, {createdAt}, {updatedAt})
        """.stripMargin
      )
        .on(
          "id"        -> account.id,
          "name"      -> account.name,
          "email"     -> account.email,
          "password"  -> account.password,
          "salt"      -> account.salt,
          "createdAt" -> account.createdAt,
          "updatedAt" -> account.updatedAt
        )
        .executeUpdate()

      account
    } {
      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.badRequest.message("Cannot create account!").data("reason", s"'$value' as '$column' is already used!")
    }

  def get(id: UUID)(implicit connection: Connection): IO[Option[Account]] =
    io {
      SQL(
        """
          |SELECT id, name, email, password, salt, created_at, updated_at
          |FROM accounts
          |WHERE id = {id}::uuid
        """.stripMargin
      )
        .on("id" -> id)
        .executeQuery()
        .as(accountRowParser.singleOpt)
    }

  def getByEmail(email: String)(implicit connection: Connection): IO[Option[Account]] =
    io {
      SQL(
        """
          |SELECT id, name, email, password, salt, created_at, updated_at
          |FROM accounts
          |WHERE email = {email}
        """.stripMargin
      )
        .on("email" -> email)
        .executeQuery()
        .as(accountRowParser.singleOpt)
    }
}
