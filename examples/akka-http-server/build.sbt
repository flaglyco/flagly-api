organization := "com.github.makiftutuncu"
name         := "switchboard-akka-http-server"
version      := "0.1.0"
scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.github.makiftutuncu" %% "switchboard-core"  % "0.1.0",
  "com.github.makiftutuncu" %% "switchboard-circe" % "0.1.0",
  "com.typesafe.akka"       %% "akka-http"         % "10.1.5",
  "de.heikoseeberger"       %% "akka-http-circe"   % "1.25.2"
)