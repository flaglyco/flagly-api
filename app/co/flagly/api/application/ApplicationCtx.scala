package co.flagly.api.application

import co.flagly.api.common.Ctx
import play.api.mvc.Request

class ApplicationCtx[A](override val request: Request[A], val application: Application) extends Ctx[A](request)
