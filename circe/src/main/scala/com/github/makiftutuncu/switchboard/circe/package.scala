package com.github.makiftutuncu.switchboard

import java.util.UUID

import io.circe.{
  Decoder => CirceDecoder,
  Encoder => CirceEncoder,
  parser  => CirceParser
}

package object circe {
  implicit def circeJsonDecoder[A](implicit circeDecoder: CirceDecoder[A]): Decoder[A] = { input: String =>
    for {
      json <- CirceParser.parse(input).toOption
      a    <- json.as[A].toOption
    } yield {
      a
    }
  }

  implicit def circeJsonEncoder[A](implicit circeEncoder: CirceEncoder[A]): Encoder[A] = { input: A =>
    circeEncoder(input).noSpaces
  }

  implicit def flagCirceDecoder[A](implicit circeDecoder: CirceDecoder[A]): CirceDecoder[Flag[A]] =
    CirceDecoder.forProduct5("id", "name", "description", "value", "defaultValue") { (id: UUID, name: String, description: String, value: A, defaultValue: A) =>
      Flag[A](id, name, description, value, defaultValue)
    }

  implicit def flagCirceEncoder[A](implicit circeEncoder: CirceEncoder[A]): CirceEncoder[Flag[A]] =
    CirceEncoder.forProduct5("id", "name", "description", "value", "defaultValue") { flag: Flag[A] =>
      (flag.id, flag.name, flag.description, flag.value, flag.defaultValue)
    }
}
