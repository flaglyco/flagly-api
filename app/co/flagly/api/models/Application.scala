package co.flagly.api.models

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, Success}
import co.flagly.api.auth.TokenUtils
import co.flagly.api.views.CreateApplication
import co.flagly.utils.ZDT
import play.api.libs.json.{Json, Writes}

final case class Application(id: UUID,
                             accountId: UUID,
                             name: String,
                             token: String,
                             createdAt: ZonedDateTime,
                             updatedAt: ZonedDateTime)

object Application {
  implicit val applicationWrites: Writes[Application] =
    Writes[Application] { application =>
      Json.obj(
        "id"        -> application.id,
        "accountId" -> application.accountId,
        "name"      -> application.name,
        "token"     -> application.token,
        "createdAt" -> ZDT.toString(application.createdAt),
        "updatedAt" -> ZDT.toString(application.updatedAt)
      )
    }

  implicit val applicationRowParser: RowParser[Application] =
    RowParser[Application] { row =>
      val id        = row[UUID]("id")
      val accountId = row[UUID]("account_id")
      val name      = row[String]("name")
      val token     = row[String]("token")
      val createdAt = row[ZonedDateTime]("created_at")
      val updatedAt = row[ZonedDateTime]("updated_at")

      Success(Application(id, accountId, name, token, createdAt, updatedAt))
    }

  def apply(accountId: UUID, createApplication: CreateApplication): Application =
    new Application(
      id        = UUID.randomUUID,
      accountId = accountId,
      name      = createApplication.name,
      token     = TokenUtils.generateToken(),
      createdAt = ZDT.now,
      updatedAt = ZDT.now
    )
}
