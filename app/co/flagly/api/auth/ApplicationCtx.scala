package co.flagly.api.auth

import co.flagly.api.models.Application
import play.api.mvc.Request

class ApplicationCtx[A](override val request: Request[A], val application: Application) extends Ctx[A](request)
