package co.flagly.api

import co.flagly.core.Flag
import play.api.libs.json.{Json, Writes}

package object flag {
  implicit val flagWrites: Writes[Flag] = Writes[Flag](flag => Json.parse(flag.toString))
}
