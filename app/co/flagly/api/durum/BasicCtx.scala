package co.flagly.api.durum

import dev.akif.durum.Ctx
import play.api.mvc.{AnyContent, Request}

final case class BasicCtx[B](override val id: String,
                             override val time: Long,
                             override val request: Request[AnyContent],
                             override val headers: Map[String, String],
                             override val body: B) extends Ctx[Request[AnyContent], B, Unit](id, time, request, headers, body, ())
