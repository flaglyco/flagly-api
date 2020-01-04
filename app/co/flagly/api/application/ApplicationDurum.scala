package co.flagly.api.application

import cats.effect.IO
import co.flagly.api.common.base.PlayDurum
import play.api.Logger
import play.api.mvc.{AnyContent, ControllerComponents, Request}

class ApplicationDurum(applicationService: ApplicationService,
                       override val logger: Logger,
                       cc: ControllerComponents) extends PlayDurum[Application, ApplicationCtx](cc) {
  override def buildAuth(request: Request[AnyContent]): IO[Application] =
    for {
      token       <- getBearerToken(request)
      application <- applicationService.getByToken(token)
    } yield {
      application
    }

  override def buildContext[IN](id: String,
                                time: Long,
                                request: Request[AnyContent],
                                headers: Map[String, String],
                                in: IN,
                                application: Application): ApplicationCtx[IN] =
    new ApplicationCtx[IN](id, time, request, headers, in, application)
}
