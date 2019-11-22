package co.flagly.api.common.base

import cats.effect.IO
import co.flagly.api.common.Errors
import dev.akif.e.E

trait Repository {
  def ioHandlingErrors[A](action: => A)(errorHandler: PartialFunction[Throwable, E]): IO[A] =
    IO.apply {
      action
    }.handleErrorWith { throwable =>
      IO.raiseError(errorHandler.applyOrElse[Throwable, E](throwable, t => Errors.database("Unhandled database error!").cause(t)))
    }

  def io[A](action: => A): IO[A] = ioHandlingErrors(action)(PartialFunction.empty)
}
