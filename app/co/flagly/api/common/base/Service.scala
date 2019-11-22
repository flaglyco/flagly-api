package co.flagly.api.common.base

import java.sql.Connection

import cats.effect.IO
import co.flagly.api.common.Errors
import dev.akif.e.E
import play.api.db.Database

import scala.util.control.NonFatal

abstract class Service(db: Database) {
  def withDBHandlingErrors[A](action: Connection => IO[A])(errorHandler: PartialFunction[Throwable, E]): IO[A] =
    useDB(autoCommit = true, action)(errorHandler)

  def withDB[A](action: Connection => IO[A]): IO[A] =
    useDB(autoCommit = true, action)(PartialFunction.empty)

  def withDBTransactionHandlingErrors[A](action: Connection => IO[A])(errorHandler: PartialFunction[Throwable, E]): IO[A] =
    useDB(autoCommit = false, action)(errorHandler)

  def withDBTransaction[A](action: Connection => IO[A]): IO[A] =
    useDB(autoCommit = false, action)(PartialFunction.empty)

  private def useDB[A](autoCommit: Boolean, action: Connection => IO[A])(errorHandler: PartialFunction[Throwable, E]): IO[A] =
    IO(db.getConnection(autoCommit)).bracket[A] { connection =>
      val io = for {
        a <- action(connection)
        _  = if (!autoCommit) connection.commit() else ()
      } yield {
        a
      }

      io.handleErrorWith { throwable =>
        val e = errorHandler.applyOrElse[Throwable, E](throwable, {
          case e: E        => e
          case NonFatal(t) => Errors.database("Unhandled database error!").cause(t)
        })

        if (!autoCommit) {
          connection.rollback()
        }

        IO.raiseError(e)
      }
    } { connection =>
      IO(connection.close())
    }
}
