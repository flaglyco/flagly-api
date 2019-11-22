package co.flagly.api.flag

import java.util.UUID

import co.flagly.api.account.{AccountCtx, AccountDürüm, AccountService}
import co.flagly.core.Flag
import play.api.mvc._

class FlagController(accountService: AccountService,
                     flagService: FlagService,
                     dürüm: AccountDürüm,
                     cc: ControllerComponents) extends AbstractController(cc) {
  import dürüm._
  import dürüm.implicits._

  def create(applicationId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithInputAndOutput[CreateFlag, Flag](request, CREATED) { ctx: AccountCtx[CreateFlag] =>
        flagService.create(applicationId, ctx.body)
      }
    }

  def get(applicationId: UUID, name: Option[String]): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithOutput[List[Flag]](request) { _: AccountCtx[Unit] =>
        name match {
          case None    => flagService.getAll(applicationId)
          case Some(n) => flagService.searchByName(applicationId, n)
        }
      }
    }

  def getById(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithOutput[Flag](request) { _: AccountCtx[Unit] =>
        flagService.get(applicationId, flagId)
      }
    }

  def update(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithInputAndOutput[UpdateFlag, Flag](request) { ctx: AccountCtx[UpdateFlag] =>
        flagService.update(applicationId, flagId, ctx.body)
      }
    }

  def delete(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      basicAction(request) { _: AccountCtx[Unit] =>
        flagService.delete(applicationId, flagId).flatMap(_ => buildResult(OK))
      }
    }
}
