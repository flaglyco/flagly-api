package co.flagly.api.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}

class RootController(cc: ControllerComponents) extends BaseController(cc) {
  val index: Action[AnyContent] =
    Action {
      Ok
    }
}
