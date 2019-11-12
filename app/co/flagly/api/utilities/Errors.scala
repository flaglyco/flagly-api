package co.flagly.api.utilities

import dev.akif.e.E
import play.api.http.Status

object Errors {
  def invalidData(message: String): E   = badRequest.name("invalid-data").message(message)
  def unauthorized(message: String): E  = unauthorized.message(message)
  def notFound(message: String): E      = notFound.message(message)
  def database(message: String): E      = internalServerError.name("database").message(message)
  def unknown(message: String): E       = internalServerError.name("unknown").message(message)

  lazy val badRequest: E          = E.of(Status.BAD_REQUEST,           "bad-request")
  lazy val unauthorized: E        = E.of(Status.UNAUTHORIZED,          "unauthorized")
  lazy val notFound: E            = E.of(Status.NOT_FOUND,             "not-found")
  lazy val internalServerError: E = E.of(Status.INTERNAL_SERVER_ERROR, "internal-server-error")
}
