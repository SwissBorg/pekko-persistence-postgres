import sbt.*

object Dependencies {
  val Scala213 = "2.13.14"
  val Scala3 = "3.3.3"
  val ScalaVersions = Seq(Scala213, Scala3)

  object Libraries {
    object Pekko {
      private val Version = "1.0.1"

      def slf4j = "org.apache.pekko" %% "pekko-slf4j" % Version
      def persistence = "org.apache.pekko" %% "pekko-persistence-query" % Version
      def persistenceTck = "org.apache.pekko" %% "pekko-persistence-tck" % Version
      def testkit = "org.apache.pekko" %% "pekko-testkit" % Version
      def streamTestkit = "org.apache.pekko" %% "pekko-stream-testkit" % Version
    }

    object Misc {
      private val ScaffeineVersion = "5.3.0"
      private val LogbackVersion = "1.5.8"
      private val PostgresqlVersion = "42.7.4"
      private val ScalaTestVersion = "3.2.19"

      def scaffeine = "com.github.blemale" %% "scaffeine" % ScaffeineVersion
      def logback = "ch.qos.logback" % "logback-classic" % LogbackVersion
      def scalatest = "org.scalatest" %% "scalatest" % ScalaTestVersion
      def postgresql = "org.postgresql" % "postgresql" % PostgresqlVersion
    }

    sealed trait Slick {
      protected val SlickVersion: String
      protected val SlickPgVersion: String

      def slick = "com.typesafe.slick" %% "slick" % SlickVersion
      def slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion
      def slickPg = "com.github.tminglei" %% "slick-pg" % SlickPgVersion
      def slickPgCirce = "com.github.tminglei" %% "slick-pg_circe-json" % SlickPgVersion
    }

    object SlickForScala2 extends Slick {
      override protected val SlickVersion = "3.4.1"
      override protected val SlickPgVersion = "0.21.1"
    }

    object SlickForScala3 extends Slick {
      override protected val SlickVersion = "3.5.1"
      override protected val SlickPgVersion = "0.22.2"
    }

    private val common: Seq[ModuleID] = Seq(
      Pekko.persistence % Provided,
      Misc.scaffeine
    )

    val scala2: Seq[ModuleID] = common ++ Seq(
      SlickForScala2.slick,
      SlickForScala2.slickHikariCP,
      SlickForScala2.slickPg,
      SlickForScala2.slickPgCirce
    )

    val scala3: Seq[ModuleID] = common ++ Seq(
      SlickForScala3.slick,
      SlickForScala3.slickHikariCP,
      SlickForScala3.slickPg,
      SlickForScala3.slickPgCirce
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
