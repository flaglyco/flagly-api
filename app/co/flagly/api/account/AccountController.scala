package co.flagly.api.account

import co.flagly.api.common.{BaseController, Ctx}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AccountController(accountService: AccountService, cc: ControllerComponents) extends BaseController(cc) {
  val register: Action[RegisterAccount] =
    publicActionWithBody[RegisterAccount] { ctx: Ctx[RegisterAccount] =>
      accountService.create(ctx.request.body).map {
        case (account, session) =>
          resultAsJson(account, Created).withHeaders(AccountCtx.sessionTokenHeaderName -> session.token)
      }
    }

  val login: Action[LoginAccount] =
    publicActionWithBody[LoginAccount] { ctx: Ctx[LoginAccount] =>
      accountService.login(ctx.request.body).map {
        case (account, session) =>
          resultAsJson(account).withHeaders(AccountCtx.sessionTokenHeaderName -> session.token)
      }
    }

  val logout: Action[AnyContent] =
    accountAction(accountService) { ctx: AccountCtx[AnyContent] =>
      accountService.logout(ctx.currentSession).map { _ =>
        Ok
      }
    }

  val me: Action[AnyContent] =
    accountAction(accountService) { implicit ctx: AccountCtx[AnyContent] =>
      Future.successful(resultAsJson(ctx.account))
    }
}
