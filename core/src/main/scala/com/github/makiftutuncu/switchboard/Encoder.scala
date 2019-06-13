package com.github.makiftutuncu.switchboard

trait Encoder[A] { self =>
  def encode(input: A): String

  def contraMap[B](f: B => A): Encoder[B] = (input: B) => self.encode(f(input))
}

object Encoder {
  def apply[A](implicit encoder: Encoder[A]): Encoder[A] = encoder

  def encode[A](input: A)(implicit encoder: Encoder[A]): String = encoder.encode(input)

  implicit class syntax[A](val input: A)(implicit encoder: Encoder[A]) {
    def encode: String = encoder.encode(input)
  }
}
