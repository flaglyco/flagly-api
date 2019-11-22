package co.flagly.api.application

import java.sql.Connection
import java.util.UUID

import anorm.SQL
import cats.effect.IO
import co.flagly.api.application.Application.applicationRowParser
import co.flagly.api.common.Errors
import co.flagly.api.common.Errors.PSQL
import co.flagly.api.common.base.Repository

class ApplicationRepository extends Repository {
  def create(application: Application)(implicit connection: Connection): IO[Application] =
    ioHandlingErrors {
      SQL(
        """
          |INSERT INTO applications(id, account_id, name, token, created_at, updated_at)
          |VALUES({id}::uuid, {accountId}::uuid, {name}, {token}, {createdAt}, {updatedAt})
        """.stripMargin
      )
        .on(
          "id"        -> application.id,
          "accountId" -> application.accountId,
          "name"      -> application.name,
          "token"     -> application.token,
          "createdAt" -> application.createdAt,
          "updatedAt" -> application.updatedAt
        )
        .executeUpdate()

      application
    } {
      case PSQL.ForeignKeyInsert(column, value, table) =>
        Errors.database("Cannot create application!").data("reason", s"'$column' with value '$value' does not exist in '$table'!")

      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot create application!").data("reason", s"'$value' as '$column' is already used!")
    }

  def getAll(accountId: UUID)(implicit connection: Connection): IO[List[Application]] =
    io {
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE account_id = {accountId}::uuid
          |ORDER BY updated_at DESC, created_at DESC
        """.stripMargin
      )
        .on("accountId" -> accountId)
        .executeQuery()
        .as(applicationRowParser.*)
    }

  def searchByName(accountId: UUID, name: String)(implicit connection: Connection): IO[List[Application]] =
    io {
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE account_id = {accountId}::uuid AND name LIKE {name}
        """.stripMargin
      )
        .on(
          "accountId" -> accountId,
          "name"      -> s"%$name%"
        )
        .executeQuery()
        .as(applicationRowParser.*)
    }

  def get(accountId: UUID, applicationId: UUID)(implicit connection: Connection): IO[Option[Application]] =
    io {
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE id = {applicationId}::uuid AND account_id = {accountId}::uuid
        """.stripMargin
      )
        .on(
          "accountId"     -> accountId,
          "applicationId" -> applicationId
        )
        .executeQuery()
        .as(applicationRowParser.singleOpt)
    }

  def getByToken(token: String)(implicit connection: Connection): IO[Option[Application]] =
    io {
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE token = {token}
        """.stripMargin
      )
        .on("token" -> token)
        .executeQuery()
        .as(applicationRowParser.singleOpt)
    }

  def update(application: Application)(implicit connection: Connection): IO[Application] =
    ioHandlingErrors {
      SQL(
        """
          |UPDATE applications
          |SET name        = {name},
          |    updated_at  = {updatedAt}
          |WHERE id = {applicationId}::uuid AND account_id = {accountId}::uuid
        """.stripMargin
      )
        .on(
          "applicationId" -> application.id,
          "accountId"     -> application.accountId,
          "name"          -> application.name,
          "updatedAt"     -> application.updatedAt
        )
        .executeUpdate()

      application
    } {
      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot update application!").data("applicationId", application.id.toString).data("accountId", application.accountId.toString).data("reason", s"'$value' as '$column' is already used!")
    }

  def delete(accountId: UUID, applicationId: UUID)(implicit connection: Connection): IO[Unit] =
    ioHandlingErrors {
      SQL(
        """
          |DELETE FROM applications
          |WHERE id = {applicationId}::uuid AND account_id = {accountId}::uuid
        """.stripMargin
      )
        .on(
          "accountId"     -> accountId,
          "applicationId" -> applicationId
        )
        .executeUpdate()
    } {
      case PSQL.ForeignKeyDelete(column, value, table) =>
        Errors.database("Cannot delete application!").data("applicationId", applicationId.toString).data("accountId", accountId.toString).data("reason", s"'$value' is still used by '$table' as '$column'!")
    } flatMap { affectedRows =>
      if (affectedRows != 1) {
        IO.raiseError(Errors.notFound("Application does not exist!"))
      } else {
        IO.unit
      }
    }
}
