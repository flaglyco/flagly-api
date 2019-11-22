package co.flagly.api.application

import cats.effect.IO
import co.flagly.api.common.base.PlayDürüm
import play.api.Logger
import play.api.mvc.{AnyContent, ControllerComponents, Request}

class ApplicationDürüm(applicationService: ApplicationService,
                       override val logger: Logger,
                       cc: ControllerComponents) extends PlayDürüm[Application, ApplicationCtx](cc) {
  override def buildAuth(request: Request[AnyContent]): IO[Application] =
    for {
      token       <- getBearerToken(request)
      application <- applicationService.getByToken(token)
    } yield {
      application
    }

  override def buildContext[IN](id: String,
                                request: Request[AnyContent],
                                headers: Map[String, String],
                                in: IN,
                                application: Application,
                                time: Long): ApplicationCtx[IN] =
    new ApplicationCtx[IN](id, request, headers, in, application, time)
}
