package co.flagly.api.controllers

import co.flagly.api.auth.Ctx
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RootController(cc: ControllerComponents) extends BaseController(cc) {
  val index: Action[AnyContent] =
    publicAction { _: Ctx[AnyContent] =>
      Future.successful(Ok)
    }
}
