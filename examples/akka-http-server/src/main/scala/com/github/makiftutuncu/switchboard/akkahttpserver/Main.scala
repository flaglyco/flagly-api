package com.github.makiftutuncu.switchboard.akkahttpserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  implicit val actorSystem: ActorSystem        = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val flagController: FlagController = new FlagController

  def main(args: Array[String]): Unit = {
    val server = Http().bindAndHandle(flagController.route, "localhost", 9000)
    println(s"Server is running on localhost:9000!")
    Await.result(server, Duration.Inf)
  }
}
