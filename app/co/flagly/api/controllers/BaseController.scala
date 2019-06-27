package co.flagly.api.controllers

import co.flagly.api.auth.{AccountCtx, Ctx}
import co.flagly.api.services.AccountService
import co.flagly.core.FlaglyError
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class BaseController(cc: ControllerComponents) extends AbstractController(cc) {
  def resultAsJson[A: Writes](a: A, status: Status = Ok): Result = status(Json.toJson(a)).as(ContentTypes.JSON)

  def publicActionWithBody[A](action: Ctx[A] => Future[Result])(implicit ec: ExecutionContext, r: Reads[A]): Action[A] =
    Action(parse.json[A]).async { request: Request[A] =>
      val ctx = new Ctx(request)
      action(ctx).map { result =>
        result.withHeaders(Ctx.requestIdHeaderName -> ctx.requestId)
      }
    }

  def publicAction(action: Ctx[AnyContent] => Future[Result])(implicit ec: ExecutionContext): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      val ctx = new Ctx(request)
      action(ctx).map { result =>
        result.withHeaders(Ctx.requestIdHeaderName -> ctx.requestId)
      }
    }

  def privateActionWithBody[A](accountService: AccountService)(action: AccountCtx[A] => Future[Result])(implicit ec: ExecutionContext, r: Reads[A]): Action[A] =
    Action(parse.json[A]).async { request: Request[A] =>
      val token = request
        .headers
        .get(HeaderNames.AUTHORIZATION)
        .map { header => if (header.startsWith("Bearer ")) header.drop(7) else header }
        .getOrElse("")

      if (token.isEmpty) {
        Future.failed(FlaglyError.of(401, "Token is missing!"))
      } else {
        accountService.getByToken(token).flatMap {
          case (account, session) =>
            val ctx = new AccountCtx(request, account, session)
            action(ctx).map { result =>
              result.withHeaders(Ctx.requestIdHeaderName -> ctx.requestId)
            }
        }.recoverWith {
          case flaglyError: FlaglyError if flaglyError.code == 401 =>
            Future.failed(FlaglyError.of(401, "Unauthorized!", flaglyError))
        }
      }
    }

  def privateAction(accountService: AccountService)(action: AccountCtx[AnyContent] => Future[Result])(implicit ec: ExecutionContext): Action[AnyContent] =
    Action.async { request: Request[AnyContent] =>
      val token = request
        .headers
        .get(HeaderNames.AUTHORIZATION)
        .map { header => if (header.startsWith("Bearer ")) header.drop(7) else header }
        .getOrElse("")

      if (token.isEmpty) {
        Future.failed(FlaglyError.of(401, "Token is missing!"))
      } else {
        accountService.getByToken(token).flatMap {
          case (account, session) =>
            val ctx = new AccountCtx(request, account, session)
            action(ctx).map { result =>
              result.withHeaders(Ctx.requestIdHeaderName -> ctx.requestId)
            }
        }.recoverWith {
          case flaglyError: FlaglyError if flaglyError.code == 401 =>
            Future.failed(FlaglyError.of(401, "Unauthorized!", flaglyError))
        }
      }
    }
}
