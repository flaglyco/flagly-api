import ReleaseTransformations._

organization in ThisBuild := "co.flagly"
scalaVersion in ThisBuild := "2.13.1"

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

resolvers += Resolver.jcenterRepo

lazy val `flagly-api` = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      evolutions,
      jdbc,
      guice,
      "co.flagly"                % "flagly-core"        % "0.2.2",
      "dev.akif"                %% "e-play-json"        % "0.2.3",
      "org.playframework.anorm" %% "anorm"              % "2.6.5",
      "org.postgresql"           % "postgresql"         % "42.2.8",
      "org.typelevel"           %% "cats-effect"        % "2.0.0",
      "org.scalatest"           %% "scalatest"          % "3.0.8" % Test,
      "org.scalatestplus.play"  %% "scalatestplus-play" % "4.0.3" % Test
    )
  )
