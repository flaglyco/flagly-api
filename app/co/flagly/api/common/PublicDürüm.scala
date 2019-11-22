package co.flagly.api.common

import cats.effect.IO
import co.flagly.api.common.base.PlayDürüm
import co.flagly.api.durum.BasicCtx
import play.api.Logger
import play.api.mvc.{AnyContent, ControllerComponents, Request}

class PublicDürüm(override val logger: Logger, cc: ControllerComponents) extends PlayDürüm[Unit, BasicCtx](cc) {
  override def buildAuth(request: Request[AnyContent]): IO[Unit] =
    IO.unit

  override def buildContext[IN](id: String,
                                request: Request[AnyContent],
                                headers: Map[String, String],
                                in: IN,
                                auth: Unit,
                                time: Long): BasicCtx[IN] =
    new BasicCtx[IN](id, request, headers, in, time)
}
