package co.flagly.api.account

import co.flagly.api.durum.Ctx
import co.flagly.api.session.Session
import play.api.mvc.{AnyContent, Request}

final case class AccountCtx[B](override val id: String,
                               override val request: Request[AnyContent],
                               override val headers: Map[String, String],
                               override val body: B,
                               account: Account,
                               session: Session,
                               override val time: Long = System.currentTimeMillis) extends Ctx[Request[AnyContent], B, (Account, Session)](id, request, headers, body, account -> session, time)

object AccountCtx {
  val sessionTokenHeaderName: String = "X-Session-Token"
}
