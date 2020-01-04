package co.flagly.api.account

import cats.effect.IO
import co.flagly.api.common.PublicDurum
import co.flagly.api.durum.BasicCtx
import co.flagly.api.session.Session
import play.api.mvc._

class AccountController(accountService: AccountService,
                        publicDurum: PublicDurum,
                        durum: AccountDurum,
                        cc: ControllerComponents) extends AbstractController(cc) {
  import durum._
  import durum.implicits._

  val register: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      // FIXME: Request is not logged when body is invalid! Probably happens for all wrap* methods
      publicDurum.wrapWithInputAndOutput[RegisterAccount, (Account, Session)](request, CREATED) { ctx: BasicCtx[RegisterAccount] =>
        accountService.create(ctx.body)
      }
    }

  val login: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      publicDurum.wrapWithInputAndOutput[LoginAccount, (Account, Session)](request) { ctx: BasicCtx[LoginAccount] =>
        accountService.login(ctx.body)
      }
    }

  val logout: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrap(request) { ctx: AccountCtx[Unit] =>
        accountService.logout(ctx.session).flatMap(_ => buildResult(OK))
      }
    }

  val me: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrapWithOutput[Account](request) { ctx: AccountCtx[Unit] =>
        IO.pure(ctx.account)
      }
    }
}
