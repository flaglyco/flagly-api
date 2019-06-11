organization in ThisBuild := "com.github.makiftutuncu"
version      in ThisBuild := "0.1"
scalaVersion in ThisBuild := "2.12.8"

lazy val core = project

lazy val circe = project.dependsOn(core).settings(
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"   % "0.11.1"
  )
)
