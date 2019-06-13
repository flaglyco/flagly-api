package com.github.makiftutuncu.switchboard

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

trait DataSource {
  def getFlag[A](id: UUID)(implicit decoder: Decoder[Flag[A]], ec: ExecutionContext): Future[Option[Flag[A]]]

  def setFlag[A](flag: Flag[A])(implicit encoder: Encoder[Flag[A]], ec: ExecutionContext): Future[Flag[A]]
}
