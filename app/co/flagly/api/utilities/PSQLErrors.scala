package co.flagly.api.utilities

import org.postgresql.util.PSQLException

object PSQLErrors {
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
