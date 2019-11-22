package co.flagly.api.utilities

import cats.effect.IO
import dev.akif.e.E

object IOExtensions {
  implicit class IOOptionExtensions[A](private val io: IO[Option[A]]) {
    def ifNoneE(e: E): IO[A] = io.flatMap {
      case None    => IO.raiseError(e)
      case Some(a) => IO.pure(a)
    }
  }
}
