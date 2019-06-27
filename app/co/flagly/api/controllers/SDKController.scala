package co.flagly.api.controllers

import co.flagly.api.auth.ApplicationCtx
import co.flagly.api.models.flagWrites
import co.flagly.api.services.{ApplicationService, FlagService}
import co.flagly.core.FlaglyError
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SDKController(applicationService: ApplicationService, flagService: FlagService, cc: ControllerComponents) extends BaseController(cc) {
  def get(name: String): Action[AnyContent] =
    applicationAction(applicationService) { ctx: ApplicationCtx[AnyContent] =>
      flagService.getByName(ctx.application.id, name).flatMap {
        case None       => Future.failed(FlaglyError.of(s"Flag '$name' of application '${ctx.application.id}' does not exist!"))
        case Some(flag) => Future.successful(resultAsJson(flag))
      }
    }
}
