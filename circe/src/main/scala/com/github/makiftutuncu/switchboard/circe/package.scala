package com.github.makiftutuncu.switchboard

import io.circe.{Json, Decoder => CirceDecoder, Encoder => CirceEncoder}

package object circe {
  type JsonDecoder[O] = Decoder[Json, O]

  type JsonEncoder[I] = Encoder[I, Json]

  implicit def jsonDecoder[O](implicit decoder: CirceDecoder[O]): JsonDecoder[O] = (input: Json) => decoder.decodeJson(input).toOption

  implicit def jsonEncoder[I](implicit encoder: CirceEncoder[I]): JsonEncoder[I] = (input: I) => encoder(input)
}
