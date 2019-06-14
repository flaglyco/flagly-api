organization in ThisBuild := "co.flagly"
version      in ThisBuild := "0.1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.8"

lazy val `flagly-api` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "co.flagly" %% "flagly-core" % "0.1.0-SNAPSHOT"
    )
  )
