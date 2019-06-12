organization in ThisBuild := "com.github.makiftutuncu"
version      in ThisBuild := "0.1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.8"

lazy val `switchboard-core` = project in file("core")

lazy val `switchboard-circe` = (project in file("circe")).dependsOn(`switchboard-core`).settings(
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"   % "0.11.1"
  )
)
