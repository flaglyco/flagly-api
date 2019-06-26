package co.flagly.api.services

import java.util.UUID

import co.flagly.api.repositories.FlagRepository
import co.flagly.api.utilities.PSQLErrors
import co.flagly.api.views.{CreateFlag, UpdateFlag}
import co.flagly.core.{Flag, FlaglyError}
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
      case PSQLErrors.ForeignKeyInsert(_, value, _) =>
        FlaglyError.of(s"Cannot create flag because application '$value' does not exist!")

      case PSQLErrors.UniqueKeyInsert(column, value) =>
        FlaglyError.of(s"Cannot create flag because '$column' as '$value' is already used!")
    }

  def getAll(applicationId: UUID)(implicit ec: ExecutionContext): Future[List[Flag]] =
    withDB { implicit connection =>
      flags.getAll(applicationId)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot get flags of application '$applicationId'!", t)
    }

  def get(applicationId: UUID, flagId: UUID)(implicit ec: ExecutionContext): Future[Option[Flag]] =
    withDB { implicit connection =>
      flags.get(applicationId, flagId)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot get flag '$flagId' of application '$applicationId'!", t)
    }

  def getByName(applicationId: UUID, name: String)(implicit ec: ExecutionContext): Future[Option[Flag]] =
    withDB { implicit connection =>
      flags.getByName(applicationId, name)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot get flag '$name' of application '$applicationId'!", t)
    }

  def update(applicationId: UUID, flagId: UUID, updateFlag: UpdateFlag)(implicit ec: ExecutionContext): Future[Flag] =
    withDBTransaction { implicit connection =>
      flags.get(applicationId, flagId) match {
        case None =>
          throw FlaglyError.of(s"Flag '$flagId' of application '$applicationId' does not exist!")

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
      case PSQLErrors.UniqueKeyInsert(column, value) =>
        FlaglyError.of(s"Cannot update flag '$flagId' of application '$applicationId' because '$column' as '$value' is already used!")

      case NonFatal(t) =>
        FlaglyError.of(s"Cannot update flag '$flagId' of application '$applicationId'!", t)
    }


  def delete(applicationId: UUID, flagId: UUID)(implicit ec: ExecutionContext): Future[Unit] =
    withDB { implicit connection =>
      flags.delete(applicationId, flagId)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot delete flag '$flagId' of application '$applicationId'!", t)
    }
}
