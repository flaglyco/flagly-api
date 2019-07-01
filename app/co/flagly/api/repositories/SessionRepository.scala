package co.flagly.api.repositories

import java.sql.Connection
import java.util.UUID

import anorm.SQL
import co.flagly.api.models.Session
import co.flagly.api.models.Session.sessionRowParser

class SessionRepository {
  def create(session: Session)(implicit connection: Connection): Session = {
    val sql =
      SQL(
        """
          |INSERT INTO sessions(id, account_id, token, created_at, updated_at)
          |VALUES({id}::uuid, {accountId}::uuid, {token}, {createdAt}, {updatedAt})
        """.stripMargin
      ).on(
        "id"        -> session.id,
        "accountId" -> session.accountId,
        "token"     -> session.token,
        "createdAt" -> session.createdAt,
        "updatedAt" -> session.updatedAt
      )

    sql.executeUpdate()

    session
  }

  def getByToken(token: String)(implicit connection: Connection): Option[Session] = {
    val sql =
      SQL(
        """
          |SELECT id, account_id, token, created_at, updated_at
          |FROM sessions
          |WHERE token = {token}
        """.stripMargin
      ).on(
        "token" -> token
      )

    sql.executeQuery().as(sessionRowParser.singleOpt)
  }

  def delete(id: UUID)(implicit connection: Connection): Unit = {
    val sql =
      SQL(
        """
          |DELETE FROM sessions
          |WHERE id = {id}::uuid
        """.stripMargin
      ).on(
        "id" -> id
      )

    sql.executeUpdate()
  }
}
