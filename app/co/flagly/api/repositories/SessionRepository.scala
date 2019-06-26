package co.flagly.api.repositories

import java.sql.Connection

import anorm.SQL
import co.flagly.api.models.Session

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
}
