package co.flagly.api.flag

import java.util.UUID

import cats.effect.IO
import co.flagly.api.common.Errors
import co.flagly.api.common.base.Service
import co.flagly.api.utilities.IOExtensions._
import co.flagly.core.Flag
import co.flagly.utils.ZDT
import play.api.db.Database

class FlagService(flags: FlagRepository, db: Database) extends Service(db) {
  def create(applicationId: UUID, createFlag: CreateFlag): IO[Flag] =
    withDB { implicit connection =>
      val flag = Flag.of(
        applicationId,
        createFlag.name,
        createFlag.description.getOrElse(""),
        createFlag.value
      )

      flags.create(flag)
    }

  def getAll(applicationId: UUID): IO[List[Flag]] =
    withDB { implicit connection =>
      flags.getAll(applicationId)
    }

  def get(applicationId: UUID, flagId: UUID): IO[Flag] =
    withDB { implicit connection =>
      flags
        .get(applicationId, flagId)
        .ifNoneE(Errors.notFound("Flag does not exist!").data("flagId", flagId.toString))
    }

  def searchByName(applicationId: UUID, name: String): IO[List[Flag]] =
    withDB { implicit connection =>
      flags.searchByName(applicationId, name)
    }

  def getByName(applicationId: UUID, name: String): IO[Flag] =
    withDB { implicit connection =>
      flags
        .getByName(applicationId, name)
        .ifNoneE(Errors.notFound("Flag does not exist!").data("name", name).data("applicationId", applicationId.toString))
    }

  def update(applicationId: UUID, flagId: UUID, updateFlag: UpdateFlag): IO[Flag] =
    withDBTransaction { implicit connection =>
      for {
        flag    <- flags.get(applicationId, flagId) ifNoneE Errors.notFound(s"Flag does not exist!")
        newFlag  = Flag.of(
                     flag.id,
                     flag.applicationId,
                     updateFlag.name.getOrElse(flag.name),
                     updateFlag.description.getOrElse(flag.description),
                     updateFlag.value.getOrElse(flag.value),
                     flag.createdAt,
                     ZDT.now
                   )
        updated <- flags.update(newFlag)
      } yield {
        updated
      }
    }


  def delete(applicationId: UUID, flagId: UUID): IO[Unit] =
    withDB { implicit connection =>
      flags.delete(applicationId, flagId)
    }
}
