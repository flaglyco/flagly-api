package co.flagly.api

import cats.effect.IO
import co.flagly.api.common.PublicDürüm
import co.flagly.api.durum.BasicCtx
import play.api.mvc._

class RootController(dürüm: PublicDürüm, cc: ControllerComponents) extends AbstractController(cc) {
  import dürüm._
  import dürüm.implicits._

  val index: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      basicAction(request) { _: BasicCtx[Unit] =>
        buildResult(OK)
      }
    }

  val ping: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithOutput[String](request) { _: BasicCtx[Unit] =>
        IO.pure("pong")
      }
    }
}
