package com.github.makiftutuncu.switchboard

trait Decoder[I, O] { self =>
  def decode(input: I): Option[O]

  def map[O2](f: O => O2): Decoder[I, O2] = flatMap(o => Option(f(o)))

  def flatMap[O2](f: O => Option[O2]): Decoder[I, O2] = { input: I => self.decode(input).flatMap(f) }
}

object Decoder {
  def apply[I, O](implicit decoder: Decoder[I, O]): Decoder[I, O] = decoder

  def decode[I, O](input: I)(implicit decoder: Decoder[I, O]): Option[O] = decoder.decode(input)

  implicit class syntax[I, O](val input: I)(implicit decoder: Decoder[I, O]) {
    def decode: Option[O] = decoder.decode(input)
  }
}
