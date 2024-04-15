import sbt.*

object Dependencies {
  val Scala213 = "2.13.13"
  val Scala3 = "3.3.3"
  val ScalaVersions = Seq(Scala213, Scala3)

  val PekkoVersion = "1.0.2"
  val ScaffeineVersion = "5.2.1"
  val ScalaTestVersion = "3.2.18"
  val SlickVersion = "3.5.1"
  val SlickPgVersion = "0.22.1"
  val LogbackVersion = "1.5.4"
  val PostgresqlVersion = "42.7.3"

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
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
    "org.postgresql" % "postgresql" % PostgresqlVersion % Test
  )
}
