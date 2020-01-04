package co.flagly.api.flag

import java.util.UUID

import co.flagly.api.account.{AccountCtx, AccountDurum, AccountService}
import co.flagly.core.Flag
import play.api.mvc._

class FlagController(accountService: AccountService,
                     flagService: FlagService,
                     durum: AccountDurum,
                     cc: ControllerComponents) extends AbstractController(cc) {
  import durum._
  import durum.implicits._

  def create(applicationId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrapWithInputAndOutput[CreateFlag, Flag](request, CREATED) { ctx: AccountCtx[CreateFlag] =>
        flagService.create(applicationId, ctx.body)
      }
    }

  def get(applicationId: UUID, name: Option[String]): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrapWithOutput[List[Flag]](request) { _: AccountCtx[Unit] =>
        name match {
          case None    => flagService.getAll(applicationId)
          case Some(n) => flagService.searchByName(applicationId, n)
        }
      }
    }

  def getById(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrapWithOutput[Flag](request) { _: AccountCtx[Unit] =>
        flagService.get(applicationId, flagId)
      }
    }

  def update(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrapWithInputAndOutput[UpdateFlag, Flag](request) { ctx: AccountCtx[UpdateFlag] =>
        flagService.update(applicationId, flagId, ctx.body)
      }
    }

  def delete(applicationId: UUID, flagId: UUID): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrap(request) { _: AccountCtx[Unit] =>
        flagService.delete(applicationId, flagId).flatMap(_ => buildResult(OK))
      }
    }
}
