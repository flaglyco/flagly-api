package co.flagly.api.common.base

import cats.effect.IO
import co.flagly.api.account.AccountCtx
import co.flagly.api.common.{EffectIOE, Errors}
import co.flagly.api.session.Session
import dev.akif.durum.{Ctx, Durum, InputBuilder, LogType, OutputBuilder, RequestLog, ResponseLog}
import dev.akif.e.E
import play.api.Logger
import play.api.http.{ContentTypes, HeaderNames, Writeable}
import play.api.libs.json._
import play.api.mvc.Results.Status
import play.api.mvc._

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.Try

abstract class PlayDurum[AUTH, CTX[BODY] <: Ctx[Request[AnyContent], BODY, AUTH]](cc: ControllerComponents) extends Durum[IO, E, Request[AnyContent], Result, AUTH, CTX]()(EffectIOE) {
  val logger: Logger

  def playAction(action: Request[AnyContent] => IO[Result]): Action[AnyContent] =
    cc.actionBuilder.async { request: Request[AnyContent] =>
      action(request).unsafeToFuture()
    }

  override val errorOutputBuilder: OutputBuilder[IO, E, Result] =
    new OutputBuilder[IO, E, Result] {
      override def build(status: Int, e: E): IO[Result] = IO.pure(Status(e.code)(e.toString).as(ContentTypes.JSON))

      override def log(e: E): IO[String] = IO.pure(e.toString)
    }

  override def getHeadersOfRequest(request: Request[AnyContent]): Map[String, String] =
    request.headers.headers.toMap

  override def getMethodOfRequest(request: Request[AnyContent]): String =
    request.method

  override def getURIOfRequest(request: Request[AnyContent]): String =
    request.uri

  override def getStatusOfResponse(result: Result): Int =
    result.header.status

  override def getStatusOfError(e: E): Int = e.code

  override def responseWithHeader(result: Result, header: (String, String)): Result =
    result.withHeaders(header)

  override def getHeadersOfResponse(result: Result): Map[String, String] =
    result.header.headers

  override def logRequest(log: RequestLog): Unit =
    if (log.failed) {
      logger.error(log.toLog(LogType.IncomingRequest))
    } else {
      logger.info(log.toLog(LogType.IncomingRequest))
    }

  override def logResponse(log: ResponseLog): Unit =
    if (log.failed) {
      logger.error(log.toLog(LogType.OutgoingResponse))
    } else {
      logger.info(log.toLog(LogType.OutgoingResponse))
    }

  def buildResult(status: Int): IO[Result] =
    IO.pure {
      Status(status)
    }

  def buildResult[O: Writeable](out: O, status: Int): IO[Result] =
    IO.pure {
      Status(status)(out)
    }

  protected def getBearerToken(request: Request[_]): IO[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) match {
      case None =>
        IO.raiseError(Errors.unauthorized("Bearer token is missing!"))

      case Some(h) if !h.startsWith("Bearer ") =>
        IO.raiseError(Errors.unauthorized("Token is not a bearer token!").data("token", h))

      case Some(header) =>
        IO.pure(header.drop(7))
    }

  object implicits {
    implicit val inputBuilderUnit: InputBuilder[IO, Request[AnyContent], Unit] =
      new InputBuilder[IO, Request[AnyContent], Unit] {
        override def build(request: Request[AnyContent]): IO[Unit] = IO.unit

        override def log(request: Request[AnyContent]): IO[String] = IO.pure("")
      }

    implicit def inputBuilderJson[A: Reads : Writes]: InputBuilder[IO, Request[AnyContent], A] =
      new InputBuilder[IO, Request[AnyContent], A] {
        override def build(request: Request[AnyContent]): IO[A] =
          getAnyContentAsStringOrJson(request.body) match {
            case Left(body) =>
              IO.raiseError(Errors.badRequest.message("Request body is invalid!").data("body", body))

            case Right(json) =>
              json.validate[A] match {
                case JsError(errors) =>
                  IO.raiseError {
                    errors.foldLeft(Errors.badRequest.message("Request body is invalid!")) {
                      case (e, (path, es)) =>
                        e.data(path.path.map(_.toJsonString).mkString, es.flatMap(_.messages).mkString(", "))
                    }
                  }

                case JsSuccess(in, _) =>
                  IO.pure(in)
              }
          }

        override def log(request: Request[AnyContent]): IO[String] =
          getAnyContentAsStringOrJson(request.body) match {
            case Left(body) =>
              IO.pure(body)

            case Right(json) =>
              json.validate[A] match {
                case JsError(_) =>
                  IO.pure(json.toString)

                case JsSuccess(a, _) =>
                  IO.pure(Json.toJson(a).toString)
              }
          }
      }

    implicit def outputBuilderJson[A: Writes]: OutputBuilder[IO, A, Result] =
      new OutputBuilder[IO, A, Result] {
        override def build(status: Int, a: A): IO[Result] =
          IO.pure {
            Status(status)(Json.toJson(a)).as(ContentTypes.JSON)
          }

        override def log(a: A): IO[String] =
          IO.pure {
            Json.toJson(a).toString()
          }
      }

    implicit def outputBuilderJsonWithSession[A: Writes : ClassTag]: OutputBuilder[IO, (A, Session), Result] =
      new OutputBuilder[IO, (A, Session), Result] {
        override def build(status: Int, tuple: (A, Session)): IO[Result] =
          tuple match {
            case (a: A, session: Session) =>
              outputBuilderJson[A].build(status, a).map { result =>
                responseWithHeader(result, AccountCtx.sessionTokenHeaderName -> session.token)
              }
          }

        override def log(tuple: (A, Session)): IO[String] =
          tuple match {
            case (a: A, _: Session) =>
              outputBuilderJson[A].log(a)
          }
      }

    @tailrec
    private final def getAnyContentAsStringOrJson(body: AnyContent): Either[String, JsValue] =
      body match {
        case AnyContentAsEmpty =>
          Left("")

        case AnyContentAsText(txt) =>
          Left(txt)

        case AnyContentAsFormUrlEncoded(form) =>
          val formAsText = form.flatMap {
            case (k, vs) =>
              vs.map(v => if (Option(v).map(_.trim).getOrElse("").isEmpty) k else s"$k=$v")
          }.mkString("&")
          Left(formAsText)

        case AnyContentAsRaw(raw) =>
          val rawAsText = raw.asBytes().mkString
          Try(Json.parse(rawAsText)).toEither.fold(_ => Left(rawAsText), j => Right(j))

        case AnyContentAsXml(xml) =>
          Left(xml.toString())

        case AnyContentAsMultipartFormData(mfd) =>
          getAnyContentAsStringOrJson(AnyContentAsFormUrlEncoded(mfd.asFormUrlEncoded))

        case AnyContentAsJson(json) =>
          Right(json)

        case b =>
          Left(b.toString)
      }
  }
}
