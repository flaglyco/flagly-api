package co.flagly.api.services

import java.util.UUID

import co.flagly.api.models.Application
import co.flagly.api.repositories.ApplicationRepository
import co.flagly.api.utilities.{Errors, PSQLErrors}
import co.flagly.api.views.{CreateApplication, UpdateApplication}
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
      case PSQLErrors.ForeignKeyInsert(column, value, table) =>
        Errors.database("Cannot create application!").data("reason", s"'$column' with value '$value' does not exist in '$table'!")

      case PSQLErrors.UniqueKeyInsert(column, value) =>
        Errors.database("Cannot create application!").data("reason", s"'$value' as '$column' is already used!")
    }

  def getAll(accountId: UUID)(implicit ec: ExecutionContext): Future[List[Application]] =
    withDB { implicit connection =>
      applications.getAll(accountId)
    } {
      case NonFatal(t) =>
        Errors.database(s"Cannot get applications!").data("accountId", accountId.toString).cause(t)
    }

  def get(accountId: UUID, applicationId: UUID)(implicit ec: ExecutionContext): Future[Option[Application]] =
    withDB { implicit connection =>
      applications.get(accountId, applicationId)
    } {
      case NonFatal(t) =>
        Errors.database(s"Cannot get application!").data("applicationId", applicationId.toString).data("accountId", accountId.toString).cause(t)
    }

  def getByName(accountId: UUID, name: String)(implicit ec: ExecutionContext): Future[Option[Application]] =
    withDB { implicit connection =>
      applications.getByName(accountId, name)
    } {
      case NonFatal(t) =>
        Errors.database(s"Cannot get application!").data("name", name).data("accountId", accountId.toString).cause(t)
    }

  def getByToken(token: String)(implicit ec: ExecutionContext): Future[Option[Application]] =
    withDB { implicit connection =>
      applications.getByToken(token)
    } {
      case NonFatal(t) =>
        Errors.database(s"Cannot get application!").data("token", token).cause(t)
    }

  def update(accountId: UUID, applicationId: UUID, updateApplication: UpdateApplication)(implicit ec: ExecutionContext): Future[Application] =
    withDBTransaction { implicit connection =>
      applications.get(accountId, applicationId) match {
        case None =>
          throw Errors.notFound("Application does not exist!")

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
        Errors.database("Cannot update application!").data("applicationId", applicationId.toString).data("accountId", accountId.toString).data("reason", s"'$value' as '$column' is already used!")

      case NonFatal(t) =>
        Errors.database("Cannot update application!").data("applicationId", applicationId.toString).data("accountId", accountId.toString).cause(t)
    }


  def delete(accountId: UUID, applicationId: UUID)(implicit ec: ExecutionContext): Future[Unit] =
    withDB { implicit connection =>
      applications.delete(accountId, applicationId)
    } {
      case PSQLErrors.ForeignKeyDelete(column, value, table) =>
        Errors.database("Cannot delete application!").data("applicationId", applicationId.toString).data("accountId", accountId.toString).data("reason", s"'$value' is still used by '$table' as '$column'!")

      case NonFatal(t) =>
        Errors.database("Cannot delete application!").data("applicationId", applicationId.toString).data("accountId", accountId.toString).cause(t)
    }
}
