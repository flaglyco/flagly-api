package co.flagly.api.application

import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

final case class CreateApplication(name: String)

object CreateApplication {
  implicit val createApplicationReads: Reads[CreateApplication] =
    Reads[CreateApplication] {
      case json: JsObject =>
        val maybeCreateApplication =
          for {
            name <- (json \ "name").asOpt[String]
          } yield {
            CreateApplication(name)
          }

        maybeCreateApplication match {
          case None       => JsError(s"$json is not a valid CreateApplication!")
          case Some(flag) => JsSuccess(flag)
        }

      case json =>
        JsError(s"$json is not a valid CreateApplication!")
    }
}
