/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres

import com.typesafe.config.{Config, ConfigFactory, ConfigValue}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.persistence.postgres.config.{JournalConfig, ReadJournalConfig}
import org.apache.pekko.persistence.postgres.db.SlickExtension
import org.apache.pekko.persistence.postgres.query.javadsl.PostgresReadJournal
import org.apache.pekko.persistence.postgres.util.DropCreate
import org.apache.pekko.serialization.SerializationExtension
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import org.apache.pekko.util.Timeout
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

abstract class SharedActorSystemTestSpec(val config: Config) extends SimpleSpec with DropCreate with BeforeAndAfterAll {
  def this(config: String = "plain-application.conf", configOverrides: Map[String, ConfigValue] = Map.empty) =
    this(configOverrides.foldLeft(ConfigFactory.load(config)) { case (conf, (path, configValue)) =>
      conf.withValue(path, configValue)
    })

  implicit lazy val system: ActorSystem = ActorSystem("test", config)
  implicit lazy val mat: Materializer = SystemMaterializer(system).materializer

  implicit lazy val ec: ExecutionContext = system.dispatcher
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 2.minutes)
  implicit val timeout: Timeout = Timeout(1.minute)

  lazy val serialization = SerializationExtension(system)

  val cfg = config.getConfig("postgres-journal")
  val journalConfig = new JournalConfig(cfg)
  lazy val db = SlickExtension(system).database(cfg).database
  val readJournalConfig = new ReadJournalConfig(config.getConfig(PostgresReadJournal.Identifier))

  override protected def afterAll(): Unit = {
    super.afterAll()
    db.close()
    system.terminate().futureValue
  }
}
