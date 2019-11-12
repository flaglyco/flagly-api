package co.flagly.api.repositories

import java.sql.Connection
import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, SQL, Success}
import co.flagly.api.utilities.Errors
import co.flagly.core.Flag

class FlagRepository {
  def create(flag: Flag)(implicit connection: Connection): Flag = {
    val sql =
      SQL(
        """
          |INSERT INTO flags(id, application_id, name, description, value, created_at, updated_at)
          |VALUES({id}::uuid, {applicationId}::uuid, {name}, {description}, {value}, {createdAt}, {updatedAt})
        """.stripMargin
      ).on(
        "id"            -> flag.id,
        "applicationId" -> flag.applicationId,
        "name"          -> flag.name,
        "description"   -> flag.description,
        "value"         -> flag.value,
        "createdAt"     -> flag.createdAt,
        "updatedAt"     -> flag.updatedAt
      )

    sql.executeUpdate()

    flag
  }

  def getAll(applicationId: UUID)(implicit connection: Connection): List[Flag] = {
    val sql =
      SQL(
        """
          |SELECT id, application_id, name, description, value, created_at, updated_at
          |FROM flags
          |WHERE application_id = {applicationId}::uuid
          |ORDER BY updated_at DESC, created_at DESC
        """.stripMargin
      ).on(
        "applicationId" -> applicationId
      )

    sql.executeQuery().as(flagRowParser.*)
  }

  def get(applicationId: UUID, flagId: UUID)(implicit connection: Connection): Option[Flag] = {
    val sql =
      SQL(
        """
          |SELECT id, application_id, name, description, value, created_at, updated_at
          |FROM flags
          |WHERE id = {flagId}::uuid AND application_id = {applicationId}::uuid
        """.stripMargin
      ).on(
        "flagId"        -> flagId,
        "applicationId" -> applicationId
      )

    sql.executeQuery().as(flagRowParser.singleOpt)
  }

  def getByName(applicationId: UUID, name: String)(implicit connection: Connection): Option[Flag] = {
    val sql =
      SQL(
        """
          |SELECT id, application_id, name, description, value, created_at, updated_at
          |FROM flags
          |WHERE application_id = {applicationId}::uuid AND name = {name}
        """.stripMargin
      ).on(
        "applicationId" -> applicationId,
        "name"          -> name
      )

    sql.executeQuery().as(flagRowParser.singleOpt)
  }

  def update(flag: Flag)(implicit connection: Connection): Flag = {
    val sql =
      SQL(
        """
          |UPDATE flags
          |SET name        = {name},
          |    description = {description},
          |    value       = {value},
          |    updated_at  = {updatedAt}
          |WHERE id = {flagId}::uuid AND application_id = {applicationId}::uuid
        """.stripMargin
      ).on(
        "flagId"        -> flag.id,
        "applicationId" -> flag.applicationId,
        "name"          -> flag.name,
        "description"   -> flag.description,
        "value"         -> flag.value,
        "updatedAt"     -> flag.updatedAt
      )

    sql.executeUpdate()

    flag
  }

  def delete(applicationId: UUID, flagId: UUID)(implicit connection: Connection): Unit = {
    val sql =
      SQL(
        """
          |DELETE FROM flags
          |WHERE id = {flagId}::uuid AND application_id = {applicationId}::uuid
        """.stripMargin
      ).on(
        "flagId"        -> flagId,
        "applicationId" -> applicationId
      )

    val affectedRows = sql.executeUpdate()

    if (affectedRows != 1) {
      throw Errors.notFound("Flag does not exist!")
    }
  }

  implicit val flagRowParser: RowParser[Flag] =
    RowParser[Flag] { row =>
      val id            = row[UUID]("id")
      val applicationId = row[UUID]("application_id")
      val name          = row[String]("name")
      val description   = row[String]("description")
      val value         = row[Boolean]("value")
      val createdAt     = row[ZonedDateTime]("created_at")
      val updatedAt     = row[ZonedDateTime]("updated_at")

      Success(Flag.of(id, applicationId, name, description, value, createdAt, updatedAt))
    }
}
