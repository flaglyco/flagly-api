package co.flagly.api.views

import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

final case class UpdateApplication(name: String)

object UpdateApplication {
  implicit val updateApplicationReads: Reads[UpdateApplication] =
    Reads[UpdateApplication] {
      case json: JsObject =>
        val maybeUpdateApplication =
          for {
            name <- (json \ "name").asOpt[String]
          } yield {
            UpdateApplication(name)
          }

        maybeUpdateApplication match {
          case None       => JsError(s"$json is not a valid UpdateApplication!")
          case Some(flag) => JsSuccess(flag)
        }

      case json =>
        JsError(s"$json is not a valid UpdateApplication!")
    }
}
