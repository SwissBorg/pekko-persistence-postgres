import sbt.*

object Dependencies {
  val Scala213 = "2.13.16"
  val Scala3 = "3.3.5"
  val ScalaVersions = Seq(Scala213, Scala3)

  object Libraries {
    object Pekko {
      private val Version = "1.1.3"

      def slf4j = "org.apache.pekko" %% "pekko-slf4j" % Version
      def persistence = "org.apache.pekko" %% "pekko-persistence-query" % Version
      def persistenceTck = "org.apache.pekko" %% "pekko-persistence-tck" % Version
      def testkit = "org.apache.pekko" %% "pekko-testkit" % Version
      def streamTestkit = "org.apache.pekko" %% "pekko-stream-testkit" % Version
    }

    object Misc {
      private val ScaffeineVersion = "5.3.0"
      private val LogbackVersion = "1.5.18"
      private val PostgresqlVersion = "42.7.5"
      private val ScalaTestVersion = "3.2.19"

      def scaffeine = "com.github.blemale" %% "scaffeine" % ScaffeineVersion
      def logback = "ch.qos.logback" % "logback-classic" % LogbackVersion
      def scalatest = "org.scalatest" %% "scalatest" % ScalaTestVersion
      def postgresql = "org.postgresql" % "postgresql" % PostgresqlVersion
    }

    object Slick {
      private val SlickVersion = "3.6.0"
      private val SlickPgVersion = "0.22.2"

      def slick = "com.typesafe.slick" %% "slick" % SlickVersion
      def slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion
      def slickPg = "com.github.tminglei" %% "slick-pg" % SlickPgVersion
      def slickPgCirce = "com.github.tminglei" %% "slick-pg_circe-json" % SlickPgVersion
    }

    val core: Seq[ModuleID] = Seq(
      Pekko.persistence % Provided,
      Misc.scaffeine,
      Slick.slick,
      Slick.slickHikariCP,
      Slick.slickPg,
      Slick.slickPgCirce
    )

    val testing: Seq[ModuleID] = Seq(
      Pekko.persistenceTck % Test,
      Pekko.slf4j % Test,
      Pekko.testkit % Test,
      Pekko.streamTestkit % Test,
      Misc.logback % Test,
      Misc.postgresql % Test,
      Misc.scalatest % Test
    )
  }
}
