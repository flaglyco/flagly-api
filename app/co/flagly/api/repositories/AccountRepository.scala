package co.flagly.api.repositories

import java.sql.Connection
import java.util.UUID

import anorm.SQL
import co.flagly.api.models.Account
import co.flagly.api.models.Account.accountRowParser

class AccountRepository {
  def create(account: Account)(implicit connection: Connection): Account = {
    val sql =
      SQL(
        """
          |INSERT INTO accounts(id, name, email, password, salt, created_at, updated_at)
          |VALUES({id}::uuid, {name}, {email}, {password}, {salt}, {createdAt}, {updatedAt})
        """.stripMargin
      ).on(
        "id"        -> account.id,
        "name"      -> account.name,
        "email"     -> account.email,
        "password"  -> account.password,
        "salt"      -> account.salt,
        "createdAt" -> account.createdAt,
        "updatedAt" -> account.updatedAt
      )

    sql.executeUpdate()

    account
  }

  def get(id: UUID)(implicit connection: Connection): Option[Account] = {
    val sql =
      SQL(
        """
          |SELECT id, name, email, password, salt, created_at, updated_at
          |FROM accounts
          |WHERE id = {id}::uuid
        """.stripMargin
      ).on(
        "id" -> id
      )

    sql.executeQuery().as(accountRowParser.singleOpt)
  }

  def getByEmail(email: String)(implicit connection: Connection): Option[Account] = {
    val sql =
      SQL(
        """
          |SELECT id, name, email, password, salt, created_at, updated_at
          |FROM accounts
          |WHERE email = {email}
        """.stripMargin
      ).on(
        "email" -> email
      )

    sql.executeQuery().as(accountRowParser.singleOpt)
  }
}
