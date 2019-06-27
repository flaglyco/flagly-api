package co.flagly.api.models

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, Success}
import co.flagly.api.auth.TokenUtils
import co.flagly.utils.ZDT

final case class Session(id: UUID,
                         accountId: UUID,
                         token: String,
                         createdAt: ZonedDateTime,
                         updatedAt: ZonedDateTime)

object Session {
  implicit val sessionRowParser: RowParser[Session] =
    RowParser[Session] { row =>
      val id        = row[UUID]("id")
      val accountId = row[UUID]("account_id")
      val token     = row[String]("token")
      val createdAt = row[ZonedDateTime]("created_at")
      val updatedAt = row[ZonedDateTime]("updated_at")

      Success(Session(id, accountId, token, createdAt, updatedAt))
    }

  def apply(accountId: UUID): Session =
    new Session(
      id        = UUID.randomUUID,
      accountId = accountId,
      token     = TokenUtils.generateToken(),
      createdAt = ZDT.now,
      updatedAt = ZDT.now
    )
}
