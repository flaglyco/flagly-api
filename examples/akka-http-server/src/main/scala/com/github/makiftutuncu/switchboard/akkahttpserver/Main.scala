package com.github.makiftutuncu.switchboard.akkahttpserver

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http => AkkaHttp}
import akka.stream.ActorMaterializer
import com.github.makiftutuncu.switchboard.redis.RedisDataSource
import com.github.makiftutuncu.switchboard.{Http, Switchboard}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

object Main {
  implicit val actorSystem: ActorSystem           = ActorSystem()
  implicit val executionContext: ExecutionContext = ExecutionContext.global
  implicit val materializer: ActorMaterializer    = ActorMaterializer()(actorSystem)

  val http: Http = new Http {}

  val redis: RedisDataSource = new RedisDataSource("localhost", 6379)

  val switchboard: Switchboard = new Switchboard(http, redis)

  val flagController: FlagController = new FlagController(switchboard)

  def main(args: Array[String]): Unit = {
    val server = AkkaHttp().bindAndHandle(flagController.route, "localhost", 9000)
    println(s"Server is running on localhost:9000!")
    Await.result(server, Duration.Inf)
  }
}
