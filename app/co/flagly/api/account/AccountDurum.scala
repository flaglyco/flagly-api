package co.flagly.api.account

import cats.effect.IO
import co.flagly.api.common.base.PlayDurum
import co.flagly.api.session.Session
import play.api.Logger
import play.api.mvc.{AnyContent, ControllerComponents, Request}

class AccountDurum(accountService: AccountService,
                   override val logger: Logger,
                   cc: ControllerComponents) extends PlayDurum[(Account, Session), AccountCtx](cc) {
  override def buildAuth(request: Request[AnyContent]): IO[(Account, Session)] =
    for {
      token       <- getBearerToken(request)
      application <- accountService.getByToken(token)
    } yield {
      application
    }

  override def buildContext[IN](id: String,
                                time: Long,
                                request: Request[AnyContent],
                                headers: Map[String, String],
                                in: IN,
                                auth: (Account, Session)): AccountCtx[IN] =
    new AccountCtx[IN](id, time, request, headers, in, auth._1, auth._2)
}
