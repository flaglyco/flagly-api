package com.github.makiftutuncu.switchboard

import java.util.UUID

import io.circe.{Json, Decoder => CirceDecoder, Encoder => CirceEncoder}

package object circe {
  type JsonDecoder[O] = Decoder[Json, O]

  type JsonEncoder[I] = Encoder[I, Json]

  implicit def jsonDecoder[O](implicit decoder: CirceDecoder[O]): JsonDecoder[O] = (input: Json) => decoder.decodeJson(input).toOption

  implicit def jsonEncoder[I](implicit encoder: CirceEncoder[I]): JsonEncoder[I] = (input: I) => encoder(input)

  implicit def flagDecoder[V](implicit decoder: CirceDecoder[V]): JsonDecoder[Flag[V]] = { input: Json =>
    val circeDecoder = CirceDecoder.forProduct5("id", "name", "description", "value", "defaultValue") { (id: UUID, name: String, description: String, value: V, defaultValue: V) =>
      Flag[V](id, name, description, value, defaultValue)
    }

    circeDecoder.decodeJson(input).toOption
  }

  implicit def flagEncoder[V](implicit encoder: CirceEncoder[V]): JsonEncoder[Flag[V]] = { input: Flag[V] =>
    val circeEncoder = CirceEncoder.forProduct5("id", "name", "description", "value", "defaultValue") { flag: Flag[V] =>
      (flag.id, flag.name, flag.description, flag.value, flag.defaultValue)
    }

    circeEncoder.apply(input)
  }
}
