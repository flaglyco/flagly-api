package co.flagly.api.flag

import java.util.UUID

import co.flagly.api.common.Errors.PSQL
import co.flagly.api.common.{BaseService, Errors}
import co.flagly.core.Flag
import co.flagly.utils.ZDT
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class FlagService(flags: FlagRepository, db: Database) extends BaseService(db) {
  def create(applicationId: UUID, createFlag: CreateFlag)(implicit ec: ExecutionContext): Future[Flag] =
    withDB { implicit connection =>
      val flag = Flag.of(
        applicationId,
        createFlag.name,
        createFlag.description.getOrElse(""),
        createFlag.value
      )

      flags.create(flag)
    } {
      case PSQL.ForeignKeyInsert(column, value, table) =>
        Errors.database("Cannot create flag!").data("reason", s"'$column' with value '$value' does not exist in '$table'!")

      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot create flag!").data("reason", s"'$value' as '$column' is already used!")
    }

  def getAll(applicationId: UUID)(implicit ec: ExecutionContext): Future[List[Flag]] =
    withDB { implicit connection =>
      flags.getAll(applicationId)
    } {
      case NonFatal(t) =>
        Errors.database("Cannot get flags!").data("applicationId", applicationId.toString).cause(t)
    }

  def get(applicationId: UUID, flagId: UUID)(implicit ec: ExecutionContext): Future[Option[Flag]] =
    withDB { implicit connection =>
      flags.get(applicationId, flagId)
    } {
      case NonFatal(t) =>
        Errors.database("Cannot get flag!").data("flagId", flagId.toString).data("applicationId", applicationId.toString).cause(t)
    }

  def getByName(applicationId: UUID, name: String)(implicit ec: ExecutionContext): Future[Option[Flag]] =
    withDB { implicit connection =>
      flags.getByName(applicationId, name)
    } {
      case NonFatal(t) =>
        Errors.database("Cannot get flag!").data("name", name).data("applicationId", applicationId.toString).cause(t)
    }

  def update(applicationId: UUID, flagId: UUID, updateFlag: UpdateFlag)(implicit ec: ExecutionContext): Future[Flag] =
    withDBTransaction { implicit connection =>
      flags.get(applicationId, flagId) match {
        case None =>
          throw Errors.notFound(s"Flag does not exist!")

        case Some(flag) =>
          val newFlag = Flag.of(
            flag.id,
            flag.applicationId,
            updateFlag.name.getOrElse(flag.name),
            updateFlag.description.getOrElse(flag.description),
            updateFlag.value.getOrElse(flag.value),
            flag.createdAt,
            ZDT.now
          )

          flags.update(newFlag)
      }
    } {
      case PSQL.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot update flag!").data("flagId", flagId.toString).data("applicationId", applicationId.toString).data("reason", s"'$value' as '$column' is already used!")

      case NonFatal(t) =>
        Errors.database("Cannot update flag!").data("flagId", flagId.toString).data("applicationId", applicationId.toString).cause(t)
    }


  def delete(applicationId: UUID, flagId: UUID)(implicit ec: ExecutionContext): Future[Unit] =
    withDB { implicit connection =>
      flags.delete(applicationId, flagId)
    } {
      case NonFatal(t) =>
        Errors.database("Cannot delete flag!").data("flagId", flagId.toString).data("applicationId", applicationId.toString).cause(t)
    }
}
