import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object ProjectAutoPlugin extends AutoPlugin {
  object autoImport {}

  override val requires = JvmPlugin
  override val trigger: PluginTrigger = allRequirements

  override def globalSettings =
    Seq(
      organization := "com.swissborg",
      organizationName := "SwissBorg",
      organizationHomepage := None,
      homepage := Some(url("https://github.com/SwissBorg/pekko-persistence-postgres")),
      licenses := Seq("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0")),
      description := "A plugin for storing events in a PostgreSQL journal",
      startYear := Some(2020),
      developers := List(
        Developer("mkubala", "Marcin Kubala", "marcin.kubala+oss@softwaremill.com", url("https://softwaremill.com"))))

  override val projectSettings: Seq[Setting[_]] = Seq(
    crossVersion := CrossVersion.binary,
    crossScalaVersions := Dependencies.ScalaVersions,
    scalaVersion := Dependencies.Scala213,
    Test / fork := true,
    Test / parallelExecution := false,
    Test / logBuffered := true,
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlog-reflective-calls",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Ydelambdafy:method",
      "-release:11"),
    Compile / doc / scalacOptions := scalacOptions.value ++ Seq(
      "-doc-title",
      "Pekko Persistence Postgres",
      "-doc-version",
      version.value,
      "-sourcepath",
      (ThisBuild / baseDirectory).value.toString,
      "-skip-packages",
      "pekko.pattern", // for some reason Scaladoc creates this
      "-doc-source-url", {
        val branch = if (isSnapshot.value) "master" else s"v${version.value}"
        s"https://github.com/SwissBorg/pekko-persistence-postgres/tree/${branch}€{FILE_PATH_EXT}#L€{FILE_LINE}"
      }),
    // show full stack traces and test case durations
    Test / testOptions += Tests.Argument("-oDF"),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/SwissBorg/pekko-persistence-postgres"),
        "git@github.com:SwissBorg/pekko-persistence-postgres.git")))

}
