package co.flagly.api.durum

import play.api.mvc.{AnyContent, Request}

final case class BasicCtx[B](override val id: String,
                             override val request: Request[AnyContent],
                             override val headers: Map[String, String],
                             override val body: B,
                             override val time: Long) extends Ctx[Request[AnyContent], B, Unit](id, request, headers, body, (), time)
