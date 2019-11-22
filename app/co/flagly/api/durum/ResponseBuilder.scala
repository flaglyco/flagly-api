package co.flagly.api.durum

import cats.effect.IO

trait ResponseBuilder[OUT, RES] {
  def build(status: Int, out: OUT): IO[RES]

  def log(out: OUT): IO[String]
}
