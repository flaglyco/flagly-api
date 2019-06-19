package co.flagly.api.errors

import scala.util.control.NoStackTrace

final case class FlaglyError(code: Int, message: String) extends Exception with NoStackTrace {
  override def getMessage: String = message
}

object FlaglyError {
  def alreadyExists(key: String, value: String): FlaglyError = FlaglyError(400, s"$key=$value already exists!")
  def doesNotExist(key: String): FlaglyError                 = FlaglyError(404, s"$key does not exist!")
  def dbOperation(message: String): FlaglyError              = FlaglyError(500, s"Database operation failed: $message")
  def dbTransaction(message: String): FlaglyError            = FlaglyError(500, s"Database transaction failed: $message")
}
