package co.flagly.api.services

import java.sql.Connection

import co.flagly.core.FlaglyError
import play.api.db.{Database, TransactionIsolationLevel}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class BaseService(db: Database) {
  def withDB[A](action: Connection => A)(errorHandler: PartialFunction[Throwable, FlaglyError])(implicit ec: ExecutionContext): Future[A] =
    Future {
      db.withConnection[A](action)
    }.recoverWith {
      case t: Throwable if errorHandler.isDefinedAt(t) => Future.failed(errorHandler(t))
      case NonFatal(t)                                 => Future.failed(FlaglyError.of("Database operation failed!", t))
    }

  def withDBTransaction[A](action: Connection => A)(errorHandler: PartialFunction[Throwable, FlaglyError])(implicit ec: ExecutionContext): Future[A] =
    Future {
      db.withTransaction[A](TransactionIsolationLevel.RepeatedRead)(action)
    }.recoverWith {
      case t: Throwable if errorHandler.isDefinedAt(t) => Future.failed(errorHandler(t))
      case NonFatal(t)                                 => Future.failed(FlaglyError.of("Database transaction failed!", t))
    }
}
