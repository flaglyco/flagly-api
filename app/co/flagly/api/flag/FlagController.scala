package co.flagly.api.flag

import java.util.UUID

import co.flagly.api.account.{AccountCtx, AccountService}
import co.flagly.api.common
import co.flagly.api.common.Errors
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FlagController(flagService: FlagService, accountService: AccountService, cc: ControllerComponents) extends common.BaseController(cc) {
  def create(applicationId: UUID): Action[CreateFlag] =
    accountActionWithBody[CreateFlag](accountService) { ctx: AccountCtx[CreateFlag] =>
      flagService.create(applicationId, ctx.request.body).map { flag =>
        resultAsJson(flag, Created)
      }
    }

  def get(applicationId: UUID, name: Option[String]): Action[AnyContent] =
    accountAction(accountService) { _: AccountCtx[AnyContent] =>
      name match {
        case None =>
          flagService.getAll(applicationId).map(flags => resultAsJson(flags))

        case Some(n) =>
          flagService.getByName(applicationId, n).flatMap {
            case None       => Future.failed(Errors.notFound("Flag does not exist!").data("name", n).data("applicationId", applicationId.toString))
            case Some(flag) => Future.successful(resultAsJson(flag))
          }
      }
    }

  def getById(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    accountAction(accountService) { _: AccountCtx[AnyContent] =>
      flagService.get(applicationId, flagId).flatMap {
        case None       => Future.failed(Errors.notFound("Flag does not exist!").data("flagId", flagId.toString))
        case Some(flag) => Future.successful(resultAsJson(flag))
      }
    }

  def update(applicationId: UUID, flagId: UUID): Action[UpdateFlag] =
    accountActionWithBody[UpdateFlag](accountService) { ctx: AccountCtx[UpdateFlag] =>
      flagService.update(applicationId, flagId, ctx.request.body).map { flag =>
        resultAsJson(flag)
      }
    }

  def delete(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    accountAction(accountService) { _: AccountCtx[AnyContent] =>
      flagService.delete(applicationId, flagId).map { _ =>
        Ok
      }
    }
}
