package co.flagly.api

import co.flagly.api.application.{ApplicationCtx, ApplicationDürüm, ApplicationService}
import co.flagly.api.flag.{FlagService, flagWrites}
import co.flagly.core.Flag
import play.api.mvc._

class SDKController(applicationService: ApplicationService,
                    flagService: FlagService,
                    dürüm: ApplicationDürüm,
                    cc: ControllerComponents) extends AbstractController(cc) {
  import dürüm._
  import dürüm.implicits._

  def get(name: String): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      actionWithOutput[Flag](request) { ctx: ApplicationCtx[Unit] =>
        flagService.getByName(ctx.application.id, name)
      }
    }
}
