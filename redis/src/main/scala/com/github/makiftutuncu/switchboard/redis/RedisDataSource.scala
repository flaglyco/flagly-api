package com.github.makiftutuncu.switchboard.redis

import java.util.UUID

import com.github.makiftutuncu.switchboard.{DataSource, Decoder, Encoder, Flag}
import com.redis.RedisClient
import com.redis.serialization.Parse

import scala.concurrent.{ExecutionContext, Future}

class RedisDataSource(val host: String, val port: Int) extends DataSource {
  val redis: RedisClient = new RedisClient(host, port)

  implicit def flagParse[A](implicit decoder: Decoder[Flag[A]]): Parse[Flag[A]] =
    Parse[Flag[A]] { bytes =>
      val input = new String(bytes, "UTF-8")
      decoder.decode(input).getOrElse(throw new Exception(s"$input is not a valid Flag"))
    }

  override def getFlag[A](id: UUID)(implicit decoder: Decoder[Flag[A]], ec: ExecutionContext): Future[Option[Flag[A]]] =
    Future {
      redis.get[Flag[A]](id.toString)
    }

  override def setFlag[A](flag: Flag[A])(implicit encoder: Encoder[Flag[A]], ec: ExecutionContext): Future[Flag[A]] =
    Future {
      redis.set(flag.id.toString, encoder.encode(flag))
      flag
    }
}
