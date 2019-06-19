package co.flagly.api.controllers

import co.flagly.api.errors.FlaglyError
import play.api.http.ContentTypes
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AbstractController, ControllerComponents, Result}

class BaseController(cc: ControllerComponents) extends AbstractController(cc) {
  def respond[A: Writes](either: Either[FlaglyError, A], status: Status = Ok): Result =
    either match {
      case Left(e)  => flaglyErrorToResult(e)
      case Right(a) => status(Json.toJson(a)).as(ContentTypes.JSON)
    }

  def respondUnit(either: Either[FlaglyError, Unit], status: Status = Ok): Result =
    either match {
      case Left(e)  => flaglyErrorToResult(e)
      case Right(_) => status
    }

  private def flaglyErrorToResult(flaglyError: FlaglyError): Result = Status(flaglyError.code)(flaglyError.message)
}
