package co.flagly.api.common

import dev.akif.e.E
import org.postgresql.util.PSQLException
import play.api.http.Status

object Errors {
  def unauthorized(message: String): E  = unauthorized.message(message)
  def notFound(message: String): E      = notFound.message(message)
  def database(message: String): E      = internalServerError.name("database").message(message)

  lazy val unexpected: E    = internalServerError.name("unexpected-error")

  lazy val badRequest: E          = E.of(Status.BAD_REQUEST,           "bad-request")
  lazy val unauthorized: E        = E.of(Status.UNAUTHORIZED,          "unauthorized")
  lazy val notFound: E            = E.of(Status.NOT_FOUND,             "not-found")
  lazy val internalServerError: E = E.of(Status.INTERNAL_SERVER_ERROR, "internal-server-error")

  def from(t: Throwable): E =
    t match {
      case e: E => e
      case _    => Errors.internalServerError.message("Unhandled error").cause(t)
    }

  object PSQL {
    object UniqueKeyInsert {
      private val regex = "Key \\((.+)\\)=\\((.+)\\) already exists".r

      def unapply(e: PSQLException): Option[(String, String)] =
        regex.findFirstMatchIn(e.getMessage).flatMap { m =>
          val matches = m.subgroups

          for {
            column <- matches.headOption
            value  <- matches.lastOption
          } yield {
            column -> value
          }
        }
    }

    object ForeignKeyInsert {
      private val regex = "Key \\((.+)\\)=\\((.+)\\) is not present in table \"(.+)\"".r

      def unapply(e: PSQLException): Option[(String, String, String)] =
        regex.findFirstMatchIn(e.getMessage).flatMap { m =>
          val matches = m.subgroups

          for {
            column <- matches.headOption
            value  <- matches.drop(1).headOption
            table  <- matches.lastOption
          } yield {
            (column, value, table)
          }
        }
    }

    object ForeignKeyDelete {
      private val regex = "Key \\((.+)\\)=\\((.+)\\) is still referenced from table \"(.+)\"".r

      def unapply(e: PSQLException): Option[(String, String, String)] =
        regex.findFirstMatchIn(e.getMessage).flatMap { m =>
          val matches = m.subgroups

          for {
            column <- matches.headOption
            value  <- matches.drop(1).headOption
            table  <- matches.lastOption
          } yield {
            (column, value, table)
          }
        }
    }
  }
}
