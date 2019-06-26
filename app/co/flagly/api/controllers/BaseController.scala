package co.flagly.api.controllers

import co.flagly.api.auth.Ctx
import play.api.http.ContentTypes
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class BaseController(cc: ControllerComponents) extends AbstractController(cc) {
  def resultAsJson[A: Writes](a: A, status: Status = Ok): Result = status(Json.toJson(a)).as(ContentTypes.JSON)

  def publicAction[A](action: Ctx[A] => Future[Result])(implicit ec: ExecutionContext, r: Reads[A]): Action[A] =
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
}
