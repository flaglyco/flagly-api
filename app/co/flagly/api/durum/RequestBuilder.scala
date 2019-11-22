package co.flagly.api.durum

import cats.effect.IO

trait RequestBuilder[REQ, IN] {
  def build(req: REQ): IO[IN]

  def log(req: REQ): IO[String]
}
