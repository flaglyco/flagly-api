package com.github.makiftutuncu.switchboard

trait Encoder[I, O] { self =>
  def encode(input: I): O

  def map[O2](f: O => O2): Encoder[I, O2] = { input: I => f(self.encode(input)) }

  def flatMap[O2](f: O => Encoder[I, O2]): Encoder[I, O2] = { input: I => f(self.encode(input)).encode(input) }

  def contraMap[I2](f: I2 => I): Encoder[I2, O] = (input: I2) => self.encode(f(input))
}

object Encoder {
  def apply[I, O](implicit encoder: Encoder[I, O]): Encoder[I, O] = encoder

  def encode[I, O](input: I)(implicit encoder: Encoder[I, O]): O = encoder.encode(input)

  implicit class syntax[I, O](val input: I)(implicit encoder: Encoder[I, O]) {
    def encode: O = encoder.encode(input)
  }
}
