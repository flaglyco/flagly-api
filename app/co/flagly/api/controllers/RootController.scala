package co.flagly.api.controllers

import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

class RootController(cc: ControllerComponents) extends AbstractController(cc) {
  val index: Action[AnyContent] =
    Action {
      Ok
    }
}
