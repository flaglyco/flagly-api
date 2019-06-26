package co.flagly.api.controllers

import co.flagly.api.auth.{AccountCtx, Ctx}
import co.flagly.api.models.Account.accountWrites
import co.flagly.api.services.AccountService
import co.flagly.api.views.CreateAccount
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

class AccountController(accountService: AccountService, cc: ControllerComponents) extends BaseController(cc) {
  val create: Action[CreateAccount] =
    publicAction[CreateAccount] { ctx: Ctx[CreateAccount] =>
      accountService.create(ctx.request.body).map {
        case (account, session) =>
          resultAsJson(account, Created).withHeaders(AccountCtx.sessionTokenHeaderName -> session.token)
      }
    }
}
