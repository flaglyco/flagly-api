organization in ThisBuild := "co.flagly"
version      in ThisBuild := "0.1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.8"

lazy val `flagly-api` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "co.flagly"              %% "flagly-scala-sdk"   % "0.1.0-SNAPSHOT",
      "org.scalatest"          %% "scalatest"          % "3.0.5"           % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0"           % Test
    )
  )
