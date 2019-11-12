package co.flagly.api.services

import java.sql.Connection

import co.flagly.api.utilities.Errors
import dev.akif.e.E
import play.api.db.{Database, TransactionIsolationLevel}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class BaseService(db: Database) {
  def withDB[A](action: Connection => A)(errorHandler: PartialFunction[Throwable, E])(implicit ec: ExecutionContext): Future[A] =
    Future {
      db.withConnection[A](action)
    }.recoverWith {
      case t: Throwable if errorHandler.isDefinedAt(t) => Future.failed(errorHandler(t))
      case NonFatal(e: E)                              => Future.failed(e)
      case NonFatal(t)                                 => Future.failed(Errors.unknown("Database operation failed!").cause(t))
    }

  def withDBTransaction[A](action: Connection => A)(errorHandler: PartialFunction[Throwable, E])(implicit ec: ExecutionContext): Future[A] =
    Future {
      db.withTransaction[A](TransactionIsolationLevel.RepeatedRead)(action)
    }.recoverWith {
      case t: Throwable if errorHandler.isDefinedAt(t) => Future.failed(errorHandler(t))
      case NonFatal(e: E)                              => Future.failed(e)
      case NonFatal(t)                                 => Future.failed(Errors.unknown("Database transaction failed!").cause(t))
    }
}
