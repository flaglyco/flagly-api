package co.flagly.api.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

@Singleton
class RootController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  val index: Action[AnyContent] =
    Action {
      Ok
    }
}
