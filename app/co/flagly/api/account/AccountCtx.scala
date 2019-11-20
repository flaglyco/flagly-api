package co.flagly.api.account

import co.flagly.api.common.Ctx
import co.flagly.api.session.Session
import play.api.mvc.Request

class AccountCtx[A](override val request: Request[A],
                    val account: Account,
                    val currentSession: Session) extends Ctx[A](request)

object AccountCtx {
  val sessionTokenHeaderName: String = "X-Session-Token"
}
