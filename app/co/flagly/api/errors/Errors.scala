package co.flagly.api.errors

import co.flagly.core.FlaglyError

object Errors {
  def alreadyExists(key: String, value: String): FlaglyError = FlaglyError.of(FlaglyError.CLIENT_ERROR_CODE, s"$key=$value already exists!")
  def doesNotExist(key: String): FlaglyError                 = FlaglyError.of(404, s"$key does not exist!")
  def dbOperation(message: String): FlaglyError              = FlaglyError.of(FlaglyError.SERVER_ERROR_CODE, s"Database operation failed: $message")
  def dbTransaction(message: String): FlaglyError            = FlaglyError.of(FlaglyError.SERVER_ERROR_CODE, s"Database transaction failed: $message")
}
