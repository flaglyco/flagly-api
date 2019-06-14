package co.flagly.api.errors

import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, NotFound}

sealed trait FlaglyError extends Exception {
  def toResult: Result
}

object FlaglyError {
  case object AlreadyExists extends FlaglyError {
    override def toResult: Result = BadRequest("Already exists!")
  }

  case object DoesNotExist  extends FlaglyError {
    override def toResult: Result = NotFound("Not found!")
  }
}
