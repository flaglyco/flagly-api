package co.flagly.api.application

import java.util.UUID

import co.flagly.api.account.{AccountCtx, AccountService}
import co.flagly.api.common.{BaseController, Errors}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationController(applicationService: ApplicationService, accountService: AccountService, cc: ControllerComponents) extends BaseController(cc) {
  val create: Action[CreateApplication] =
    accountActionWithBody[CreateApplication](accountService) { ctx: AccountCtx[CreateApplication] =>
      applicationService.create(ctx.account.id, ctx.request.body).map { application =>
        resultAsJson(application, Created)
      }
    }

  def get(name: Option[String]): Action[AnyContent] =
    accountAction(accountService) { ctx: AccountCtx[AnyContent] =>
      name match {
        case None =>
          applicationService.getAll(ctx.account.id).map(applications => resultAsJson(applications))

        case Some(n) =>
          applicationService.getByName(ctx.account.id, n).flatMap {
            case None              => Future.failed(Errors.notFound("Application does not exist!").data("name", n))
            case Some(application) => Future.successful(resultAsJson(application))
          }
      }
    }

  def getById(applicationId: UUID): Action[AnyContent] =
    accountAction(accountService) { ctx: AccountCtx[AnyContent] =>
      applicationService.get(ctx.account.id, applicationId).flatMap {
        case None              => Future.failed(Errors.notFound("Application does not exist!").data("applicationId", applicationId.toString))
        case Some(application) => Future.successful(resultAsJson(application))
      }
    }

  def update(applicationId: UUID): Action[UpdateApplication] =
    accountActionWithBody[UpdateApplication](accountService) { ctx: AccountCtx[UpdateApplication] =>
      applicationService.update(ctx.account.id, applicationId, ctx.request.body).map { application =>
        resultAsJson(application)
      }
    }

  def delete(applicationId: UUID): Action[AnyContent] =
    accountAction(accountService) { ctx: AccountCtx[AnyContent] =>
      applicationService.delete(ctx.account.id, applicationId).map { _ =>
        Ok
      }
    }
}
