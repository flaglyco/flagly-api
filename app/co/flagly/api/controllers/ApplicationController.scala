package co.flagly.api.controllers

import java.util.UUID

import co.flagly.api.auth.Ctx
import co.flagly.api.services.ApplicationService
import co.flagly.api.views.{CreateApplication, UpdateApplication}
import co.flagly.core.FlaglyError
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationController(applicationService: ApplicationService, cc: ControllerComponents) extends BaseController(cc) {
  // TODO: Remove when `privateAction` is implemented to take care of building an `AccountCtx` (instead of `Ctx`) from the request!
  val accountId: UUID = UUID.fromString("646d4ed2-7d34-42e2-84a8-cd8116915f8d")

  val create: Action[CreateApplication] =
    publicAction[CreateApplication] { ctx: Ctx[CreateApplication] =>
      applicationService.create(accountId, ctx.request.body).map { application =>
        resultAsJson(application, Created)
      }
    }

  def get(name: Option[String]): Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      name match {
        case None =>
          applicationService.getAll(accountId).map(applications => resultAsJson(applications))

        case Some(n) =>
          applicationService.getByName(accountId, n).flatMap {
            case None              => Future.failed(FlaglyError.of(s"Application '$n' does not exist!"))
            case Some(application) => Future.successful(resultAsJson(application))
          }
      }
    }

  def getById(applicationId: UUID): Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      applicationService.get(accountId, applicationId).flatMap {
        case None              => Future.failed(FlaglyError.of(s"Application '$applicationId' does not exist!"))
        case Some(application) => Future.successful(resultAsJson(application))
      }
    }

  def update(applicationId: UUID): Action[UpdateApplication] =
    publicAction[UpdateApplication] { ctx: Ctx[UpdateApplication] =>
      applicationService.update(accountId, applicationId, ctx.request.body).map { application =>
        resultAsJson(application)
      }
    }

  def delete(applicationId: UUID): Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      applicationService.delete(accountId, applicationId).map { _ =>
        Ok
      }
    }
}
