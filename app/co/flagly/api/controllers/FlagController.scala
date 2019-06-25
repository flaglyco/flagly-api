package co.flagly.api.controllers

import java.util.UUID

import co.flagly.api.errors.Errors
import co.flagly.api.models.{CreateFlag, UpdateFlag}
import co.flagly.api.services.FlagService
import co.flagly.core.FlagJson.flagWrites
import play.api.mvc._

class FlagController(flagService: FlagService, cc: ControllerComponents) extends BaseController(cc) {
  val create: Action[CreateFlag] =
    Action(parse.json[CreateFlag]) { request: Request[CreateFlag] =>
      respond(flagService.create(request.body), Created)
    }

  def get(name: Option[String]): Action[AnyContent] =
    Action {
      name match {
        case None =>
          respond(flagService.getAll)

        case Some(n) =>
          respond(
            flagService.getByName(n).flatMap {
              case None       => Left(Errors.doesNotExist(s"Flag $n"))
              case Some(flag) => Right(flag)
            }
          )
      }
    }

  def getById(id: UUID): Action[AnyContent] =
    Action {
      respond(
        flagService.get(id).flatMap {
          case None       => Left(Errors.doesNotExist(s"Flag $id"))
          case Some(flag) => Right(flag)
        }
      )
    }

  def update(id: UUID): Action[UpdateFlag] =
    Action(parse.json[UpdateFlag]) { request: Request[UpdateFlag] =>
      respond(flagService.update(id, request.body))
    }

  def delete(id: UUID): Action[AnyContent] =
    Action {
      respondUnit(flagService.delete(id))
    }
}
