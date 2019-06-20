organization in ThisBuild := "co.flagly"
version      in ThisBuild := "0.1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.8"

lazy val `flagly-api` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      evolutions,
      jdbc,
      guice,
      "co.flagly"               %% "flagly-core"        % "0.1.0-SNAPSHOT",
      "org.playframework.anorm" %% "anorm"              % "2.6.2",
      "org.postgresql"           % "postgresql"         % "42.2.5",
      "org.scalatest"           %% "scalatest"          % "3.0.5" % Test,
      "org.scalatestplus.play"  %% "scalatestplus-play" % "4.0.0" % Test
    )
  )
