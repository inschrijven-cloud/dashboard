import ReleaseTransformations._
import GhPagesKeys._

name := "speelsysteem-dashboard"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  coverageExcludedPackages := """controllers\..*Reverse.*;router.Routes.*;""",
  dockerRepository in Docker := Some("thomastoye")
//  dockerUpdateLatest in Docker := true // waiting for https://github.com/playframework/playframework/pull/6924
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    packageName in Docker := "speelsysteem-dashboard"
  )
  .enablePlugins(PlayScala)
  .dependsOn(dataAccess)
  .aggregate(dataAccess)

scalaVersion := "2.11.8"

coverageEnabled in Test := true

libraryDependencies ++= Seq(
  filters,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)

routesGenerator := InjectedRoutesGenerator

lazy val models = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.5.6",

      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    ),
    publishLocal in Docker := { },
    publish in Docker := { }
  )

lazy val dataAccess = Project("data-access", file("data-access"))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      "com.ibm" %% "couchdb-scala" % "0.7.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
      "com.norbitltd" % "spoiwo" % "1.0.3",

      "org.scalatest" %% "scalatest" % "3.0.0" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.3.0" % "test"
    ),
    publishLocal in Docker := { },
    publish in Docker := { }
  )
  .dependsOn(models)
  .aggregate(models)

ghpages.settings
enablePlugins(SiteScaladocPlugin)

siteSourceDirectory := file("app/site")

val publishScalaDoc = (ref: ProjectRef) => ReleaseStep(
  action = releaseStepTaskAggregated(GhPagesKeys.pushSite in ref) // publish scaladoc
)

releaseProcess <<= thisProjectRef apply { ref =>
  import sbtrelease.ReleaseStateTransformations._

  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    //publishArtifacts,
    publishScalaDoc(ref),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
}

git.remoteRepo := "git@github.com:speelsysteem/dashboard.git"

releaseIgnoreUntrackedFiles := true
