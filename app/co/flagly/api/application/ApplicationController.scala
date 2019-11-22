package co.flagly.api.application

import java.util.UUID

import co.flagly.api.account.{AccountCtx, AccountDürüm, AccountService}
import play.api.mvc._

class ApplicationController(accountService: AccountService,
                            applicationService: ApplicationService,
                            dürüm: AccountDürüm,
                            cc: ControllerComponents) extends AbstractController(cc) {
  import dürüm._
  import dürüm.implicits._

  val create: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithInputAndOutput[CreateApplication, Application](request, CREATED) { ctx: AccountCtx[CreateApplication] =>
        applicationService.create(ctx.account.id, ctx.body)
      }
    }

  def get(name: Option[String]): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithOutput[List[Application]](request) { ctx: AccountCtx[Unit] =>
        name match {
          case None    => applicationService.getAll(ctx.account.id)
          case Some(n) => applicationService.searchByName(ctx.account.id, n)
        }
      }
    }

  def getById(applicationId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithOutput[Application](request) { ctx: AccountCtx[Unit] =>
        applicationService.get(ctx.account.id, applicationId)
      }
    }

  def update(applicationId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithInputAndOutput[UpdateApplication, Application](request) { ctx: AccountCtx[UpdateApplication] =>
        applicationService.update(ctx.account.id, applicationId, ctx.body)
      }
    }

  def delete(applicationId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      basicAction(request) { ctx: AccountCtx[Unit] =>
        applicationService.delete(ctx.account.id, applicationId).flatMap(_ => buildResult(OK))
      }
    }
}
