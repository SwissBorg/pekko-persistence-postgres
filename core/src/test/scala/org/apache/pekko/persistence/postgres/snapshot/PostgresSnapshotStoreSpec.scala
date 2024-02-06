/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.snapshot

import org.apache.pekko.persistence.postgres.config._
import org.apache.pekko.persistence.postgres.db.SlickDatabase
import org.apache.pekko.persistence.postgres.util.Schema._
import org.apache.pekko.persistence.postgres.util.{ClasspathResources, DropCreate}
import org.apache.pekko.persistence.snapshot.SnapshotStoreSpec
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

abstract class PostgresSnapshotStoreSpec(schemaType: SchemaType)
    extends SnapshotStoreSpec(ConfigFactory.load(schemaType.configName))
    with BeforeAndAfterAll
    with ScalaFutures
    with ClasspathResources
    with DropCreate {
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 10.seconds)

  implicit lazy val ec: ExecutionContextExecutor = system.dispatcher

  lazy val cfg = system.settings.config.getConfig("postgres-journal")

  lazy val journalConfig = new JournalConfig(cfg)

  lazy val db = SlickDatabase.database(cfg, new SlickConfiguration(cfg.getConfig("slick")), "slick.db")

  override def beforeAll(): Unit = {
    dropCreate(schemaType)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    db.close()
  }
}

class PlainSnapshotStoreSpec extends PostgresSnapshotStoreSpec(Plain)
