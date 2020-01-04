package co.flagly.api.account

import co.flagly.api.session.Session
import dev.akif.durum.Ctx
import play.api.mvc.{AnyContent, Request}

final case class AccountCtx[B](override val id: String,
                               override val time: Long,
                               override val request: Request[AnyContent],
                               override val headers: Map[String, String],
                               override val body: B,
                               account: Account,
                               session: Session) extends Ctx[Request[AnyContent], B, (Account, Session)](id, time, request, headers, body, account -> session)

object AccountCtx {
  val sessionTokenHeaderName: String = "X-Session-Token"
}
