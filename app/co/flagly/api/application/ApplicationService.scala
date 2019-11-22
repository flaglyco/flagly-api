package co.flagly.api.application

import java.util.UUID

import cats.effect.IO
import co.flagly.api.common.Errors
import co.flagly.api.common.base.Service
import co.flagly.api.utilities.IOExtensions._
import co.flagly.utils.ZDT
import play.api.db.Database

class ApplicationService(applications: ApplicationRepository, db: Database) extends Service(db) {
  def create(accountId: UUID, createApplication: CreateApplication): IO[Application] =
    withDB { implicit connection =>
      applications.create(Application(accountId, createApplication))
    }

  def getAll(accountId: UUID): IO[List[Application]] =
    withDB { implicit connection =>
      applications.getAll(accountId)
    }

  def searchByName(accountId: UUID, name: String): IO[List[Application]] =
    withDB { implicit connection =>
      applications.searchByName(accountId, name)
    }

  def get(accountId: UUID, applicationId: UUID): IO[Application] =
    withDB { implicit connection =>
      applications
        .get(accountId, applicationId)
        .ifNoneE(Errors.notFound("Application does not exist!").data("applicationId", applicationId.toString))
    }

  def getByToken(token: String): IO[Application] =
    withDB { implicit connection =>
      applications
        .getByToken(token)
        .ifNoneE(Errors.unauthorized("Bearer token is invalid!"))
    }

  def update(accountId: UUID, applicationId: UUID, updateApplication: UpdateApplication): IO[Application] =
    withDBTransaction { implicit connection =>
      for {
        application    <- applications.get(accountId, applicationId) ifNoneE Errors.notFound("Application does not exist!")
        newApplication  = Application(
                            id        = applicationId,
                            accountId = accountId,
                            name      = updateApplication.name,
                            token     = application.token,
                            createdAt = application.createdAt,
                            updatedAt = ZDT.now
                          )
        updated        <- applications.update(newApplication)
      } yield {
        updated
      }
    }


  def delete(accountId: UUID, applicationId: UUID): IO[Unit] =
    withDB { implicit connection =>
      applications.delete(accountId, applicationId)
    }
}
