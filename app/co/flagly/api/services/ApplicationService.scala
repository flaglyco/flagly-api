package co.flagly.api.services

import java.util.UUID

import co.flagly.api.models.Application
import co.flagly.api.repositories.ApplicationRepository
import co.flagly.api.utilities.PSQLErrors
import co.flagly.api.views.{CreateApplication, UpdateApplication}
import co.flagly.core.FlaglyError
import co.flagly.utils.ZDT
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ApplicationService(applications: ApplicationRepository, db: Database) extends BaseService(db) {
  def create(accountId: UUID, createApplication: CreateApplication)(implicit ec: ExecutionContext): Future[Application] =
    withDB { implicit connection =>
      val application = Application(accountId, createApplication)

      applications.create(application)
    } {
      case PSQLErrors.ForeignKeyInsert(_, value, _) =>
        FlaglyError.of(s"Cannot create application because account '$value' does not exist!")

      case PSQLErrors.UniqueKeyInsert(column, value) =>
        FlaglyError.of(s"Cannot create application because '$column' as '$value' is already used!")
    }

  def getAll(accountId: UUID)(implicit ec: ExecutionContext): Future[List[Application]] =
    withDB { implicit connection =>
      applications.getAll(accountId)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot get applications of account '$accountId'!", t)
    }

  def get(accountId: UUID, applicationId: UUID)(implicit ec: ExecutionContext): Future[Option[Application]] =
    withDB { implicit connection =>
      applications.get(accountId, applicationId)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot get application '$applicationId' of account '$accountId'!", t)
    }

  def getByName(accountId: UUID, name: String)(implicit ec: ExecutionContext): Future[Option[Application]] =
    withDB { implicit connection =>
      applications.getByName(accountId, name)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot get application '$name' of account '$accountId'!", t)
    }

  def getByToken(token: String)(implicit ec: ExecutionContext): Future[Option[Application]] =
    withDB { implicit connection =>
      applications.getByToken(token)
    } {
      case NonFatal(t) =>
        FlaglyError.of(s"Cannot get application for token '$token'!", t)
    }

  def update(accountId: UUID, applicationId: UUID, updateApplication: UpdateApplication)(implicit ec: ExecutionContext): Future[Application] =
    withDBTransaction { implicit connection =>
      applications.get(accountId, applicationId) match {
        case None =>
          throw FlaglyError.of(s"Application '$applicationId' of account '$accountId' does not exist!")

        case Some(application) =>
          val newApplication = Application(
            id        = applicationId,
            accountId = accountId,
            name      = updateApplication.name,
            token     = application.token,
            createdAt = application.createdAt,
            updatedAt = ZDT.now
          )

          applications.update(newApplication)
      }
    } {
      case PSQLErrors.UniqueKeyInsert(column, value) =>
        FlaglyError.of(s"Cannot update application '$applicationId' of account '$accountId' because '$column' as '$value' is already used!")

      case NonFatal(t) =>
        FlaglyError.of(s"Cannot update application '$applicationId' of account '$accountId'!", t)
    }


  def delete(accountId: UUID, applicationId: UUID)(implicit ec: ExecutionContext): Future[Unit] =
    withDB { implicit connection =>
      applications.delete(accountId, applicationId)
    } {
      case PSQLErrors.ForeignKeyDelete(column, value, table) =>
        FlaglyError.of(s"Cannot delete application '$applicationId' of account '$accountId' because '$column' as '$value' is still used by '$table'!")

      case NonFatal(t) =>
        FlaglyError.of(s"Cannot delete application '$applicationId' of account '$accountId'!", t)
    }
}
