package co.flagly.api.auth

import co.flagly.api.models.{Account, Session}
import play.api.mvc.Request

class AccountCtx[A](override val request: Request[A],
                    val account: Account,
                    val currentSession: Session) extends Ctx[A](request)

object AccountCtx {
  val sessionTokenHeaderName: String = "X-Session-Token"
}
