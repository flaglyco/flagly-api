package co.flagly.api

import cats.effect.IO
import co.flagly.api.common.PublicDurum
import co.flagly.api.durum.BasicCtx
import play.api.mvc._

class RootController(durum: PublicDurum, cc: ControllerComponents) extends AbstractController(cc) {
  import durum._
  import durum.implicits._

  val index: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrap(request) { _: BasicCtx[Unit] =>
        buildResult(OK)
      }
    }

  val ping: Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrapWithOutput[String](request) { _: BasicCtx[Unit] =>
        IO.pure("pong")
      }
    }
}
