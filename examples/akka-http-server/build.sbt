organization := "com.github.makiftutuncu"
name         := "sb-akka-http-server"
version      := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.github.makiftutuncu" %% "sb-core"         % "0.1.0-SNAPSHOT",
  "com.github.makiftutuncu" %% "sb-circe"        % "0.1.0-SNAPSHOT",
  "com.typesafe.akka"       %% "akka-http"       % "10.1.5",
  "de.heikoseeberger"       %% "akka-http-circe" % "1.25.2"
)
