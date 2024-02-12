import sbt._

object Dependencies {
  val Scala213 = "2.13.12"
  val ScalaVersions = Seq(Scala213)

  val PekkoVersion = "1.0.2"
  val FlywayVersion = "9.20.0"
  val ScaffeineVersion = "5.2.1"
  val ScalaTestVersion = "3.2.17"
  val SlickVersion = "3.4.1"
  val SlickPgVersion = "0.21.1"
  val SslConfigVersion = "0.6.1"

  val LogbackVersion = "1.4.14"

  val JdbcDrivers = Seq("org.postgresql" % "postgresql" % "42.7.1")

  val Libraries: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % LogbackVersion % Test,
    "com.github.blemale" %% "scaffeine" % ScaffeineVersion,
    "com.github.tminglei" %% "slick-pg" % SlickPgVersion,
    "com.github.tminglei" %% "slick-pg_circe-json" % SlickPgVersion,
    "org.apache.pekko" %% "pekko-slf4j" % PekkoVersion % Test,
    "org.apache.pekko" %% "pekko-persistence-tck" % PekkoVersion % Test,
    "org.apache.pekko" %% "pekko-stream-testkit" % PekkoVersion % Test,
    "org.apache.pekko" %% "pekko-testkit" % PekkoVersion % Test,
    "org.apache.pekko" %% "pekko-persistence-query" % PekkoVersion % Provided,
    "com.typesafe.slick" %% "slick" % SlickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test) ++ JdbcDrivers.map(_ % Test)
}
