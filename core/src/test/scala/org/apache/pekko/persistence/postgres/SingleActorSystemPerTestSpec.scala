/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres

import com.typesafe.config.{Config, ConfigFactory, ConfigValue}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.persistence.postgres.config.{JournalConfig, ReadJournalConfig, SlickConfiguration}
import org.apache.pekko.persistence.postgres.db.SlickDatabase
import org.apache.pekko.persistence.postgres.query.javadsl.PostgresReadJournal
import org.apache.pekko.persistence.postgres.util.DropCreate
import org.apache.pekko.util.Timeout
import org.scalatest.BeforeAndAfterEach
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.duration.*

abstract class SingleActorSystemPerTestSpec(val config: Config)
    extends SimpleSpec
    with DropCreate
    with BeforeAndAfterEach {
  def this(config: String = "plain-application.conf", configOverrides: Map[String, ConfigValue] = Map.empty) =
    this(configOverrides.foldLeft(ConfigFactory.load(config)) { case (conf, (path, configValue)) =>
      conf.withValue(path, configValue)
    })

  implicit val pc: PatienceConfig = PatienceConfig(timeout = 2.minutes)
  implicit val timeout: Timeout = Timeout(1.minute)

  val cfg: Config = config.getConfig("postgres-journal")
  val journalConfig = new JournalConfig(cfg)
  val readJournalConfig = new ReadJournalConfig(config.getConfig(PostgresReadJournal.Identifier))

  // The db is initialized in the before and after each bocks
  var dbOpt: Option[Database] = None
  def db: Database = {
    dbOpt.getOrElse {
      val newDb = if (cfg.hasPath("slick.profile")) {
        SlickDatabase.database(cfg, new SlickConfiguration(cfg.getConfig("slick")), "slick.db")
      } else
        SlickDatabase.database(
          config,
          new SlickConfiguration(config.getConfig("pekko-persistence-postgres.shared-databases.slick")),
          "pekko-persistence-postgres.shared-databases.slick.db"
        )

      dbOpt = Some(newDb)
      newDb
    }
  }

  def closeDb(): Unit = {
    dbOpt.foreach(_.close())
    dbOpt = None
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    closeDb()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    closeDb()
  }

  def withActorSystem[A](f: ActorSystem => A): Unit = {
    implicit val system: ActorSystem = ActorSystem("test", config)
    f(system)
    system.terminate().futureValue
  }

  def withActorSystem[A](config: Config = config)(f: ActorSystem => A): Unit = {
    implicit val system: ActorSystem = ActorSystem("test", config)
    f(system)
    system.terminate().futureValue
  }
}
