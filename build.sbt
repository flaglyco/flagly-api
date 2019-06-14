organization in ThisBuild := "com.github.makiftutuncu"
version      in ThisBuild := "0.1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.8"

lazy val `sb-core` = project in file("core")

lazy val `sb-circe` = (project in file("circe")).dependsOn(`sb-core`).settings(
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"   % "0.11.1",
    "io.circe" %% "circe-parser" % "0.11.1"
  )
)

lazy val `sb-play-json` = (project in file("play-json")).dependsOn(`sb-core`).settings(
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.7.3"
  )
)

lazy val `sb-redis` = (project in file("redis")).dependsOn(`sb-core`).settings(
  libraryDependencies ++= Seq(
    "net.debasishg" %% "redisclient" % "3.9"
  )
)
