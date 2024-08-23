import sbt.*
import sbt.Keys.*
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
        Developer("mkubala", "Marcin Kubala", "marcin.kubala+oss@softwaremill.com", url("https://softwaremill.com"))
      )
    )

  override val projectSettings: Seq[Setting[_]] = commonCrossCompileSettings ++ Seq(
    Test / fork := true,
    Test / parallelExecution := false,
    Test / logBuffered := true,
    Compile / doc / scalacOptions := scalacOptions.value ++ Seq(
      "-doc-title",
      "Pekko Persistence Postgres",
      "-doc-version",
      version.value,
      "-sourcepath",
      (ThisBuild / baseDirectory).value.toString,
      "-doc-source-url", {
        val branch = if (isSnapshot.value) "main" else s"v${version.value}"
        s"https://github.com/SwissBorg/pekko-persistence-postgres/tree/${branch}€{FILE_PATH_EXT}#L€{FILE_LINE}"
      }
    ),
    // show full stack traces and test case durations
    Test / testOptions += Tests.Argument("-oDF"),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/SwissBorg/pekko-persistence-postgres"),
        "git@github.com:SwissBorg/pekko-persistence-postgres.git"
      )
    )
  )

  private lazy val commonScalacOptions: Seq[String] = Seq(
    "-encoding",
    "UTF-8", // Specify character encoding used by source files.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-release:11"
  )

  private def commonCrossCompileSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := Dependencies.Scala3,
    crossScalaVersions := Seq(Dependencies.Scala213, Dependencies.Scala3),
    scalacOptions ++= {
      (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) =>
          commonScalacOptions ++ Seq(
            // options dedicated for cross build / migration to Scala 3
            "-source:3.0-migration"
          )
        case _ =>
          commonScalacOptions ++ Seq(
            "-Xsource:3"
          )
      })
    }
  )
}
