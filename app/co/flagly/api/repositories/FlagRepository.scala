package co.flagly.api.repositories

import java.time.ZonedDateTime
import java.util.UUID

import anorm.{RowParser, SQL, Success}
import co.flagly.api.errors.Errors
import co.flagly.core.{Flag, FlaglyError}
import play.api.db.Database

class FlagRepository(db: Database) extends Repository(db) {
  def create(flag: Flag): Either[FlaglyError, Flag] =
    withConnection { implicit connection =>
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

      val affectedRows = sql.executeUpdate()

      if (affectedRows != 1) {
        val error = Errors.dbOperation(s"cannot create flag, affected $affectedRows rows")
        logger.error(error.message)
        Left(error)
      } else {
        Right(flag)
      }
    }

  def getAll(applicationId: UUID): Either[FlaglyError, List[Flag]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, application_id, name, description, value, created_at, updated_at
            |FROM flags
            |WHERE application_id = {applicationId}
          """.stripMargin
        ).on(
          "applicationId" -> applicationId
        )

      val flags = sql.executeQuery().as(flagRowParser.*)

      Right(flags)
    }

  def get(applicationId: UUID, flagId: UUID): Either[FlaglyError, Option[Flag]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, name, description, value, created_at, updated_at
            |FROM flags
            |WHERE id = {flagId}::uuid AND application_id = {applicationId}::uuid
          """.stripMargin
        ).on(
          "flagId"        -> flagId,
          "applicationId" -> applicationId
        )

      val maybeFlag = sql.executeQuery().as(flagRowParser.singleOpt)

      Right(maybeFlag)
    }

  def getByName(applicationId: UUID, name: String): Either[FlaglyError, Option[Flag]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, name, description, value, created_at, updated_at
            |FROM flags
            |WHERE application_id = {applicationId} AND name = {name}
          """.stripMargin
        ).on(
          "applicationId" -> applicationId,
          "name"          -> name
        )

      val maybeFlag = sql.executeQuery().as(flagRowParser.singleOpt)

      Right(maybeFlag)
    }

  def update(applicationId: UUID, flagId: UUID, updater: Flag => Flag): Either[FlaglyError, Flag] =
    withTransaction { implicit connection =>
      get(applicationId, flagId).flatMap {
        case None =>
          val error = Errors.dbOperation(s"cannot update flag $flagId, it does not exist")
          logger.error(error.message)
          Left(error)

        case Some(flag) =>
          val newFlag = updater(flag)

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
              "flagId"        -> flagId,
              "applicationId" -> applicationId,
              "name"          -> newFlag.name,
              "description"   -> newFlag.description,
              "value"         -> newFlag.value,
              "updatedAt"     -> newFlag.updatedAt
            )

          val affectedRows = sql.executeUpdate()

          if (affectedRows != 1) {
            val error = Errors.dbTransaction(s"cannot update flag $flagId, affected $affectedRows rows")
            logger.error(error.message)
            connection.rollback()
            Left(error)
          } else {
            Right(newFlag)
          }
      }
    }

  def delete(applicationId: UUID, flagId: UUID): Either[FlaglyError, Unit] =
    withConnection { implicit connection =>
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
        val error = Errors.dbOperation(s"cannot delete flag $flagId, affected $affectedRows rows")
        logger.error(error.message)
        Left(error)
      } else {
        Right(())
      }
    }

  implicit val flagRowParser: RowParser[Flag] =
    RowParser[Flag] { row =>
      val id            = row[UUID]("id")
      val applicationId = row[UUID]("applicationId")
      val name          = row[String]("name")
      val description   = row[String]("description")
      val value         = row[Boolean]("value")
      val createdAt     = row[ZonedDateTime]("created_at")
      val updatedAt     = row[ZonedDateTime]("updated_at")

      Success(Flag.of(id, applicationId, name, description, value, createdAt, updatedAt))
    }
}
