package co.flagly.api.controllers

import java.util.UUID

import co.flagly.api.errors.FlaglyError
import co.flagly.api.models.{CreateFlag, UpdateFlag}
import co.flagly.api.services.FlagService
import javax.inject.{Inject, Singleton}
import play.api.http.ContentTypes
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request, Result}

@Singleton
class FlagController @Inject()(flagService: FlagService,
                               cc: ControllerComponents) extends AbstractController(cc) {
  val create: Action[CreateFlag] =
    Action(parse.json[CreateFlag]) { request: Request[CreateFlag] =>
      respond(flagService.create(request.body), Created)
    }

  val getAll: Action[AnyContent] =
    Action {
      respond(flagService.getAll)
    }

  def get(id: UUID): Action[AnyContent] =
    Action {
      respond(
        flagService.get(id).flatMap {
          case None       => Left(FlaglyError.DoesNotExist)
          case Some(flag) => Right(flag)
        }
      )
    }

  def update(id: UUID): Action[UpdateFlag] =
    Action(parse.json[UpdateFlag]) { request: Request[UpdateFlag] =>
      respond(flagService.update(id, request.body))
    }

  def delete(id: UUID): Action[AnyContent] =
    Action {
      respondUnit(flagService.delete(id))
    }

  private def respond[A: Writes](either: Either[FlaglyError, A], status: Status = Ok): Result =
    either match {
      case Left(e)  => e.toResult
      case Right(a) => status(Json.toJson(a)).as(ContentTypes.JSON)
    }

  private def respondUnit(either: Either[FlaglyError, Unit], status: Status = Ok): Result =
    either match {
      case Left(e)  => e.toResult
      case Right(_) => status
    }
}
