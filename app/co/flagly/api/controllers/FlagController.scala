package co.flagly.api.controllers

import java.util.UUID

import co.flagly.api.errors.Errors
import co.flagly.api.models.{CreateFlag, UpdateFlag}
import co.flagly.api.services.FlagService
import co.flagly.core.FlagJson.flagWrites
import play.api.mvc._

class FlagController(flagService: FlagService, cc: ControllerComponents) extends BaseController(cc) {
  def create(applicationId: UUID): Action[CreateFlag] =
    Action(parse.json[CreateFlag]) { request: Request[CreateFlag] =>
      respond(flagService.create(applicationId, request.body), Created)
    }

  def get(applicationId: UUID, name: Option[String]): Action[AnyContent] =
    Action {
      name match {
        case None =>
          respond(flagService.getAll(applicationId))

        case Some(n) =>
          respond(
            flagService.getByName(applicationId, n).flatMap {
              case None       => Left(Errors.doesNotExist(s"Flag $n"))
              case Some(flag) => Right(flag)
            }
          )
      }
    }

  def getById(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    Action {
      respond(
        flagService.get(applicationId, flagId).flatMap {
          case None       => Left(Errors.doesNotExist(s"Flag $flagId"))
          case Some(flag) => Right(flag)
        }
      )
    }

  def update(applicationId: UUID, flagId: UUID): Action[UpdateFlag] =
    Action(parse.json[UpdateFlag]) { request: Request[UpdateFlag] =>
      respond(flagService.update(applicationId, flagId, request.body))
    }

  def delete(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    Action {
      respondUnit(flagService.delete(applicationId, flagId))
    }
}
