package co.flagly.api.controllers

import java.util.UUID

import co.flagly.api.errors.FlaglyError
import co.flagly.api.models.FlagExtensions.flagWrites
import co.flagly.api.models.{CreateFlag, UpdateFlag}
import co.flagly.api.services.FlagService
import play.api.mvc._

class FlagController(flagService: FlagService, cc: ControllerComponents) extends BaseController(cc) {
  val create: Action[CreateFlag] =
    Action(parse.json[CreateFlag]) { request: Request[CreateFlag] =>
      respond(flagService.create(request.body), Created)
    }

  val getAll: Action[AnyContent] =
    Action {
      respond(flagService.getAll)
    }

  def get(id: UUID): Action[AnyContent] =
    Action {
      respond(
        flagService.get(id).flatMap {
          case None       => Left(FlaglyError.doesNotExist(s"Flag $id"))
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
