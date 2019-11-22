package co.flagly.api.session

import java.sql.Connection
import java.util.UUID

import anorm.SQL
import cats.effect.IO
import co.flagly.api.common.base.Repository
import co.flagly.api.session.Session.sessionRowParser

class SessionRepository extends Repository {
  def create(session: Session)(implicit connection: Connection): IO[Session] =
    io {
      SQL(
        """
          |INSERT INTO sessions(id, account_id, token, created_at, updated_at)
          |VALUES({id}::uuid, {accountId}::uuid, {token}, {createdAt}, {updatedAt})
        """.stripMargin
      )
        .on(
          "id"        -> session.id,
          "accountId" -> session.accountId,
          "token"     -> session.token,
          "createdAt" -> session.createdAt,
          "updatedAt" -> session.updatedAt
        )
        .executeUpdate()

      session
    }

  def getByToken(token: String)(implicit connection: Connection): IO[Option[Session]] =
    io {
      SQL(
        """
          |SELECT id, account_id, token, created_at, updated_at
          |FROM sessions
          |WHERE token = {token}
        """.stripMargin
      )
        .on("token" -> token)
        .executeQuery()
        .as(sessionRowParser.singleOpt)
    }

  def delete(id: UUID)(implicit connection: Connection): IO[Unit] =
    io {
      SQL(
        """
          |DELETE FROM sessions
          |WHERE id = {id}::uuid
        """.stripMargin
      )
        .on("id" -> id)
        .executeUpdate()
    }
}
