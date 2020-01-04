package co.flagly.api.common

import cats.effect.IO
import dev.akif.durum.Effect
import dev.akif.e.E

object EffectIOE extends Effect[IO, E] {
  override def pure[A](a: A): IO[A] = IO.pure(a)

  override def error[A](e: E): IO[A] = IO.raiseError(e)

  override def map[A, B](f: IO[A])(m: A => B): IO[B] = f.map(m)

  override def flatMap[A, B](f: IO[A])(fm: A => IO[B]): IO[B] = f.flatMap(fm)

  override def foreach[A, U](f: IO[A])(fe: A => U): Unit = f.map(fe)

  override def fold[A, B](f: IO[A])(handleError: E => IO[B], fm: A => IO[B]): IO[B] =
    f.redeemWith(
      t => t match {
        case e: E => error[B](e)
        case t    => error[B](Errors.unexpected.cause(t))
      },
      a => fm(a)
    )

  override def mapError[A, AA >: A](f: IO[A])(handleError: E => IO[AA]): IO[AA] =
    f.redeemWith(
      t => t match {
        case e: E => handleError(e)
        case t    => error[AA](Errors.unexpected.cause(t))
      },
      a => pure[AA](a)
    )
}
