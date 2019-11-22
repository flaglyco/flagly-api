package co.flagly.api.account

import cats.effect.IO
import co.flagly.api.common.PublicDürüm
import co.flagly.api.durum.BasicCtx
import co.flagly.api.session.Session
import play.api.mvc._

class AccountController(accountService: AccountService,
                        publicDürüm: PublicDürüm,
                        dürüm: AccountDürüm,
                        cc: ControllerComponents) extends AbstractController(cc) {
  import dürüm._
  import dürüm.implicits._

  val register: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      publicDürüm.actionWithInputAndOutput[RegisterAccount, (Account, Session)](request, CREATED) { ctx: BasicCtx[RegisterAccount] =>
        accountService.create(ctx.body)
      }
    }

  val login: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      publicDürüm.actionWithInputAndOutput[LoginAccount, (Account, Session)](request) { ctx: BasicCtx[LoginAccount] =>
        accountService.login(ctx.body)
      }
    }

  val logout: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      basicAction(request) { ctx: AccountCtx[Unit] =>
        accountService.logout(ctx.session).flatMap(_ => buildResult(OK))
      }
    }

  val me: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithOutput[Account](request) { ctx: AccountCtx[Unit] =>
        IO.pure(ctx.account)
      }
    }
}
