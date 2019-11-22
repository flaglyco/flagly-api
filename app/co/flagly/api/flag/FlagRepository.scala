package co.flagly.api.flag

import java.sql.Connection
import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, SQL, Success}
import cats.effect.IO
import co.flagly.api.common.Errors
import co.flagly.api.common.Errors.PSQL
import co.flagly.api.common.base.Repository
import co.flagly.core.Flag

class FlagRepository extends Repository {
  def create(flag: Flag)(implicit connection: Connection): IO[Flag] =
    ioHandlingErrors {
      SQL(
        """
          |INSERT INTO flags(id, application_id, name, description, value, created_at, updated_at)
          |VALUES({id}::uuid, {applicationId}::uuid, {name}, {description}, {value}, {createdAt}, {updatedAt})
        """.stripMargin
      )
        .on(
          "id"            -> flag.id,
          "applicationId" -> flag.applicationId,
          "name"          -> flag.name,
          "description"   -> flag.description,
          "value"         -> flag.value,
          "createdAt"     -> flag.createdAt,
          "updatedAt"     -> flag.updatedAt
        )
        .executeUpdate()

      flag
    } {
      case PSQL.ForeignKeyInsert(column, value, table) =>
        Errors.database("Cannot create flag!").data("reason", s"'$column' with value '$value' does not exist in '$table'!")

      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot create flag!").data("reason", s"'$value' as '$column' is already used!")
    }

  def getAll(applicationId: UUID)(implicit connection: Connection): IO[List[Flag]] =
    io {
      SQL(
        """
          |SELECT id, application_id, name, description, value, created_at, updated_at
          |FROM flags
          |WHERE application_id = {applicationId}::uuid
          |ORDER BY updated_at DESC, created_at DESC
        """.stripMargin
      )
        .on("applicationId" -> applicationId)
        .executeQuery()
        .as(flagRowParser.*)
    }

  def get(applicationId: UUID, flagId: UUID)(implicit connection: Connection): IO[Option[Flag]] =
    io {
      SQL(
        """
          |SELECT id, application_id, name, description, value, created_at, updated_at
          |FROM flags
          |WHERE id = {flagId}::uuid AND application_id = {applicationId}::uuid
        """.stripMargin
      )
        .on(
          "flagId"        -> flagId,
          "applicationId" -> applicationId
        )
        .executeQuery()
        .as(flagRowParser.singleOpt)
    }

  def searchByName(applicationId: UUID, name: String)(implicit connection: Connection): IO[List[Flag]] =
    io {
      SQL(
        """
          |SELECT id, application_id, name, description, value, created_at, updated_at
          |FROM flags
          |WHERE application_id = {applicationId}::uuid AND name LIKE {name}
        """.stripMargin
      )
        .on(
          "applicationId" -> applicationId,
          "name"          -> s"%$name%"
        )
        .executeQuery()
        .as(flagRowParser.*)
    }

  def getByName(applicationId: UUID, name: String)(implicit connection: Connection): IO[Option[Flag]] =
    io {
      SQL(
        """
          |SELECT id, application_id, name, description, value, created_at, updated_at
          |FROM flags
          |WHERE application_id = {applicationId}::uuid AND name = {name}
        """.stripMargin
      )
        .on(
          "applicationId" -> applicationId,
          "name"          -> name
        )
        .executeQuery()
        .as(flagRowParser.singleOpt)
    }

  def update(flag: Flag)(implicit connection: Connection): IO[Flag] =
    ioHandlingErrors {
      SQL(
        """
          |UPDATE flags
          |SET name        = {name},
          |    description = {description},
          |    value       = {value},
          |    updated_at  = {updatedAt}
          |WHERE id = {flagId}::uuid AND application_id = {applicationId}::uuid
        """.stripMargin
      )
        .on(
          "flagId"        -> flag.id,
          "applicationId" -> flag.applicationId,
          "name"          -> flag.name,
          "description"   -> flag.description,
          "value"         -> flag.value,
          "updatedAt"     -> flag.updatedAt
        )
        .executeUpdate()

      flag
    } {
      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot update flag!").data("flagId", flag.id.toString).data("applicationId", flag.applicationId.toString).data("reason", s"'$value' as '$column' is already used!")
    }

  def delete(applicationId: UUID, flagId: UUID)(implicit connection: Connection): IO[Unit] =
    io {
      SQL(
        """
          |DELETE FROM flags
          |WHERE id = {flagId}::uuid AND application_id = {applicationId}::uuid
        """.stripMargin
      )
        .on(
          "flagId"        -> flagId,
          "applicationId" -> applicationId
        )
        .executeUpdate()
    } flatMap { affectedRows =>
      if (affectedRows != 1) {
        IO.raiseError(Errors.notFound("Flag does not exist!"))
      } else {
        IO.unit
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
