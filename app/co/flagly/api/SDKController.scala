package co.flagly.api

import co.flagly.api.application.{ApplicationCtx, ApplicationDurum, ApplicationService}
import co.flagly.api.flag.{FlagService, flagWrites}
import co.flagly.core.Flag
import play.api.mvc._

class SDKController(applicationService: ApplicationService,
                    flagService: FlagService,
                    durum: ApplicationDurum,
                    cc: ControllerComponents) extends AbstractController(cc) {
  import durum._
  import durum.implicits._

  def get(name: String): Action[AnyContent] =
    playAction { request: Request[AnyContent] =>
      wrapWithOutput[Flag](request) { ctx: ApplicationCtx[Unit] =>
        flagService.getByName(ctx.application.id, name)
      }
    }
}
