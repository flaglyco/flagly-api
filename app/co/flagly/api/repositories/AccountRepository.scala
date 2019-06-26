package co.flagly.api.repositories

import java.sql.Connection

import anorm.SQL
import co.flagly.api.models.Account

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
}
