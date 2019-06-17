package co.flagly.api.models

import co.flagly.data.{DataType, Flag}
import play.api.libs.json.{JsValue, Json, Reads}

final case class CreateFlag(name: String,
                            description: Option[String],
                            dataType: DataType,
                            value: JsValue) { self =>
  def toFlag: Option[Flag] =
    dataType match {
      case DataType.Boolean => self.value.asOpt[Boolean].map    { value => Flag.boolean(self.name, self.description.getOrElse(""), value) }
      case DataType.Number  => self.value.asOpt[BigDecimal].map { value => Flag.number(self.name, self.description.getOrElse(""), value) }
      case DataType.Text    => self.value.asOpt[String].map     { value => Flag.text(self.name, self.description.getOrElse(""), value) }
    }
}

object CreateFlag {
  implicit val createFlagReads: Reads[CreateFlag] = Json.reads[CreateFlag]
}
