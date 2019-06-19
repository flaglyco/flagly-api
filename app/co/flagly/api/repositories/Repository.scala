package co.flagly.api.repositories

import java.sql.Connection

import co.flagly.api.errors.FlaglyError
import org.postgresql.util.PSQLException
import play.api.Logging
import play.api.db.{Database, TransactionIsolationLevel}

import scala.util.control.NonFatal

class Repository(db: Database) extends Logging {
  def withConnection[A](action: Connection => Either[FlaglyError, A]): Either[FlaglyError, A] =
    try {
      db.withConnection(action)
    } catch {
      case Repository.UniqueKeyInsertViolation(key, value) =>
        val error = FlaglyError.alreadyExists(key, value)
        logger.warn(error.message)
        Left(error)

      case NonFatal(t) =>
        val error = FlaglyError.dbOperation(t.getMessage)
        logger.error(error.message, t)
        Left(error)
    }

  def withTransaction[A](action: Connection => Either[FlaglyError, A]): Either[FlaglyError, A] =
    try {
      db.withTransaction(TransactionIsolationLevel.RepeatedRead)(action)
    } catch {
      case NonFatal(t) =>
        val error = FlaglyError.dbTransaction(t.getMessage)
        logger.error(error.message, t)
        Left(error)
    }
}

object Repository {
  object UniqueKeyInsertViolation {
    private val regex = "Key \\((.+)\\)=\\((.+)\\) already exists".r

    def unapply(e: PSQLException): Option[(String, String)] =
      regex.findFirstMatchIn(e.getMessage).flatMap { m =>
        val matches = m.subgroups

        for {
          key   <- matches.headOption
          value <- matches.lastOption
        } yield {
          key -> value
        }
      }
  }

  object ForeignKeyInsertViolation {
    private val regex = "Key \\((.+)\\)=\\((.+)\\) is not present in table \"(.+)\"".r

    def unapply(e: PSQLException): Option[(String, String, String)] =
      regex.findFirstMatchIn(e.getMessage).flatMap { m =>
        val matches = m.subgroups

        for {
          key   <- matches.headOption
          value <- matches.drop(1).headOption
          table <- matches.lastOption
        } yield {
          (key, value, table)
        }
      }
  }

  object ForeignKeyDeleteViolation {
    private val regex = "Key \\((.+)\\)=\\((.+)\\) is still referenced from table \"(.+)\"".r

    def unapply(e: PSQLException): Option[(String, String, String)] =
      regex.findFirstMatchIn(e.getMessage).flatMap { m =>
        val matches = m.subgroups

        for {
          key   <- matches.headOption
          value <- matches.drop(1).headOption
          table <- matches.lastOption
        } yield {
          (key, value, table)
        }
      }
  }
}
