package co.flagly.api.application

import dev.akif.durum.Ctx
import play.api.mvc.{AnyContent, Request}

final case class ApplicationCtx[B](override val id: String,
                                   override val time: Long,
                                   override val request: Request[AnyContent],
                                   override val headers: Map[String, String],
                                   override val body: B,
                                   application: Application) extends Ctx[Request[AnyContent], B, Application](id, time, request, headers, body, application)
