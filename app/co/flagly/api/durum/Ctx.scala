package co.flagly.api.durum

import java.util.UUID

abstract class Ctx[REQ, +B, A](val id: String,
                               val request: REQ,
                               val headers: Map[String, String],
                               val body: B,
                               val auth: A,
                               val time: Long)

object Ctx {
  val requestIdHeaderName: String = "X-Request-Id"

  def getOrCreateId(headers: Map[String, String]): String = headers.getOrElse(requestIdHeaderName, UUID.randomUUID.toString)
}
