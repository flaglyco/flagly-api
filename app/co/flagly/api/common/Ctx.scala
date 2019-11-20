package co.flagly.api.common

import java.util.UUID

import play.api.mvc.{Request, WrappedRequest}

class Ctx[A](val request: Request[A]) extends WrappedRequest[A](request) {
  val requestId: String = request.headers.get(Ctx.requestIdHeaderName).getOrElse(UUID.randomUUID.toString)
}

object Ctx {
  val requestIdHeaderName: String = "X-Request-Id"
}
