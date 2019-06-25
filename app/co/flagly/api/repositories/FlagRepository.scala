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
            |INSERT INTO flags(id, name, description, value, created_at, updated_at)
            |VALUES({id}::uuid, {name}, {description}, {value}, {createdAt}, {updatedAt})
          """.stripMargin
        ).on(
          "id"          -> flag.id,
          "name"        -> flag.name,
          "description" -> flag.description,
          "value"       -> flag.value,
          "createdAt"   -> flag.createdAt,
          "updatedAt"   -> flag.updatedAt
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

  def getAll: Either[FlaglyError, List[Flag]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, name, description, value, created_at, updated_at
            |FROM flags
          """.stripMargin
        )

      val flags = sql.executeQuery().as(flagRowParser.*)

      Right(flags)
    }

  def get(id: UUID): Either[FlaglyError, Option[Flag]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, name, description, value, created_at, updated_at
            |FROM flags
            |WHERE id = {id}::uuid
          """.stripMargin
        ).on(
          "id" -> id
        )

      val maybeFlag = sql.executeQuery().as(flagRowParser.singleOpt)

      Right(maybeFlag)
    }

  def getByName(name: String): Either[FlaglyError, Option[Flag]] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |SELECT id, name, description, value, created_at, updated_at
            |FROM flags
            |WHERE name = {name}
          """.stripMargin
        ).on(
          "name" -> name
        )

      val maybeFlag = sql.executeQuery().as(flagRowParser.singleOpt)

      Right(maybeFlag)
    }

  def update(id: UUID, updater: Flag => Flag): Either[FlaglyError, Flag] =
    withTransaction { implicit connection =>
      get(id).flatMap {
        case None =>
          val error = Errors.dbOperation(s"cannot update flag $id, it does not exist")
          logger.error(error.message)
          Left(error)

        case Some(flag) =>
          val newFlag = updater(flag)

          val sql =
            SQL(
              """
                |UPDATE flags
                |SET name = {name},
                |    description = {description},
                |    value = {value},
                |    updated_at = {updatedAt}
                |WHERE id = {id}::uuid
              """.stripMargin
            ).on(
              "id"          -> newFlag.id,
              "name"        -> newFlag.name,
              "description" -> newFlag.description,
              "value"       -> newFlag.value,
              "updatedAt"   -> newFlag.updatedAt
            )

          val affectedRows = sql.executeUpdate()

          if (affectedRows != 1) {
            val error = Errors.dbTransaction(s"cannot update flag $id, affected $affectedRows rows")
            logger.error(error.message)
            connection.rollback()
            Left(error)
          } else {
            Right(newFlag)
          }
      }
    }

  def delete(id: UUID): Either[FlaglyError, Unit] =
    withConnection { implicit connection =>
      val sql =
        SQL(
          """
            |DELETE FROM flags
            |WHERE id = {id}::uuid
          """.stripMargin
        ).on(
          "id" -> id
        )

      val affectedRows = sql.executeUpdate()

      if (affectedRows != 1) {
        val error = Errors.dbOperation(s"cannot delete flag $id, affected $affectedRows rows")
        logger.error(error.message)
        Left(error)
      } else {
        Right(())
      }
    }

  implicit val flagRowParser: RowParser[Flag] =
    RowParser[Flag] { row =>
      val id          = row[UUID]("id")
      val name        = row[String]("name")
      val description = row[String]("description")
      val value       = row[Boolean]("value")
      val createdAt   = row[ZonedDateTime]("created_at")
      val updatedAt   = row[ZonedDateTime]("updated_at")

      Success(Flag.of(id, name, description, value, createdAt, updatedAt))
    }
}
