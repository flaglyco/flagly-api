package co.flagly.api.application

import co.flagly.api.durum.Ctx
import play.api.mvc.{AnyContent, Request}

final case class ApplicationCtx[B](override val id: String,
                                   override val request: Request[AnyContent],
                                   override val headers: Map[String, String],
                                   override val body: B,
                                   application: Application,
                                   override val time: Long = System.currentTimeMillis) extends Ctx[Request[AnyContent], B, Application](id, request, headers, body, application, time)
