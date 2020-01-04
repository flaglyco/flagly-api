package co.flagly.api.common

import cats.effect.IO
import co.flagly.api.common.base.PlayDurum
import co.flagly.api.durum.BasicCtx
import play.api.Logger
import play.api.mvc.{AnyContent, ControllerComponents, Request}

class PublicDurum(override val logger: Logger, cc: ControllerComponents) extends PlayDurum[Unit, BasicCtx](cc) {
  override def buildAuth(request: Request[AnyContent]): IO[Unit] =
    IO.unit

  override def buildContext[IN](id: String,
                                time: Long,
                                request: Request[AnyContent],
                                headers: Map[String, String],
                                in: IN,
                                auth: Unit): BasicCtx[IN] =
    new BasicCtx[IN](id, time, request, headers, in)
}
