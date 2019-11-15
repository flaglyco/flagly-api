package co.flagly.api.repositories

import java.sql.Connection
import java.util.UUID

import anorm.SQL
import co.flagly.api.models.Application
import co.flagly.api.models.Application.applicationRowParser
import co.flagly.api.utilities.Errors

class ApplicationRepository {
  def create(application: Application)(implicit connection: Connection): Application = {
    val sql =
      SQL(
        """
          |INSERT INTO applications(id, account_id, name, token, created_at, updated_at)
          |VALUES({id}::uuid, {accountId}::uuid, {name}, {token}, {createdAt}, {updatedAt})
        """.stripMargin
      ).on(
        "id"        -> application.id,
        "accountId" -> application.accountId,
        "name"      -> application.name,
        "token"     -> application.token,
        "createdAt" -> application.createdAt,
        "updatedAt" -> application.updatedAt
      )

    sql.executeUpdate()

    application
  }

  def getAll(accountId: UUID)(implicit connection: Connection): List[Application] = {
    val sql =
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE account_id = {accountId}::uuid
          |ORDER BY updated_at DESC, created_at DESC
        """.stripMargin
      ).on(
        "accountId" -> accountId
      )

    sql.executeQuery().as(applicationRowParser.*)
  }

  def get(accountId: UUID, applicationId: UUID)(implicit connection: Connection): Option[Application] = {
    val sql =
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE id = {applicationId}::uuid AND account_id = {accountId}::uuid
        """.stripMargin
      ).on(
        "accountId"     -> accountId,
        "applicationId" -> applicationId
      )

    sql.executeQuery().as(applicationRowParser.singleOpt)
  }

  def getByName(accountId: UUID, name: String)(implicit connection: Connection): Option[Application] = {
    val sql =
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE account_id = {accountId}::uuid AND name = {name}
        """.stripMargin
      ).on(
        "accountId" -> accountId,
        "name"      -> name
      )

    sql.executeQuery().as(applicationRowParser.singleOpt)
  }

  def getByToken(token: String)(implicit connection: Connection): Option[Application] = {
    val sql =
      SQL(
        """
          |SELECT id, account_id, name, token, created_at, updated_at
          |FROM applications
          |WHERE token = {token}
        """.stripMargin
      ).on(
        "token" -> token
      )

    sql.executeQuery().as(applicationRowParser.singleOpt)
  }

  def update(application: Application)(implicit connection: Connection): Application = {
    val sql =
      SQL(
        """
          |UPDATE applications
          |SET name        = {name},
          |    updated_at  = {updatedAt}
          |WHERE id = {applicationId}::uuid AND account_id = {accountId}::uuid
        """.stripMargin
      ).on(
        "applicationId" -> application.id,
        "accountId"     -> application.accountId,
        "name"          -> application.name,
        "updatedAt"     -> application.updatedAt
      )

    sql.executeUpdate()

    application
  }

  def delete(accountId: UUID, applicationId: UUID)(implicit connection: Connection): Unit = {
    val sql =
      SQL(
        """
          |DELETE FROM applications
          |WHERE id = {applicationId}::uuid AND account_id = {accountId}::uuid
        """.stripMargin
      ).on(
        "accountId"     -> accountId,
        "applicationId" -> applicationId
      )

    val affectedRows = sql.executeUpdate()

    if (affectedRows != 1) {
      throw Errors.notFound("Application does not exist!")
    }
  }
}
