/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.journal

import org.apache.pekko.actor.Props
import org.apache.pekko.persistence.CapabilityFlag
import org.apache.pekko.persistence.journal.JournalPerfSpec
import org.apache.pekko.persistence.journal.JournalPerfSpec.{ BenchActor, Cmd, ResetCounter }
import org.apache.pekko.persistence.postgres.config._
import org.apache.pekko.persistence.postgres.db.SlickExtension
import org.apache.pekko.persistence.postgres.util.Schema._
import org.apache.pekko.persistence.postgres.util.{ ClasspathResources, DropCreate }
import org.apache.pekko.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

abstract class PostgresJournalPerfSpec(config: String, schemaType: SchemaType)
    extends JournalPerfSpec(ConfigFactory.load(config))
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with ScalaFutures
    with ClasspathResources
    with DropCreate {
  override protected def supportsRejectingNonSerializableObjects: CapabilityFlag = true

  implicit lazy val ec: ExecutionContextExecutor = system.dispatcher

  implicit def pc: PatienceConfig = PatienceConfig(timeout = 10.minutes)

  override def eventsCount: Int = 100

  override def awaitDurationMillis: Long = 10.minutes.toMillis

  override def measurementIterations: Int = 1

  lazy val cfg = system.settings.config.getConfig("postgres-journal")

  lazy val journalConfig = new JournalConfig(cfg)

  lazy val db = SlickExtension(system).database(cfg).database

  override def beforeAll(): Unit = {
    dropCreate(schemaType)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    db.close()
    super.afterAll()
  }

  def actorCount = 100

  private val commands = Vector(1 to eventsCount: _*)

  "A PersistentActor's performance" must {
    s"measure: persist()-ing $eventsCount events for $actorCount actors" in {
      val testProbe = TestProbe()
      val replyAfter = eventsCount
      def createBenchActor(actorNumber: Int) =
        system.actorOf(Props(classOf[BenchActor], s"$pid--$actorNumber", testProbe.ref, replyAfter))
      val actors = 1.to(actorCount).map(createBenchActor)

      measure(d => s"Persist()-ing $eventsCount * $actorCount took ${d.toMillis} ms") {
        for (cmd <- commands; actor <- actors) {
          actor ! Cmd("p", cmd)
        }
        for (_ <- actors) {
          testProbe.expectMsg(awaitDurationMillis.millis, commands.last)
        }
        for (actor <- actors) {
          actor ! ResetCounter
        }
      }
    }
  }

  "A PersistentActor's performance" must {
    s"measure: persistAsync()-ing $eventsCount events for $actorCount actors" in {
      val testProbe = TestProbe()
      val replyAfter = eventsCount
      def createBenchActor(actorNumber: Int) =
        system.actorOf(Props(classOf[BenchActor], s"$pid--$actorNumber", testProbe.ref, replyAfter))
      val actors = 1.to(actorCount).map(createBenchActor)

      measure(d => s"persistAsync()-ing $eventsCount * $actorCount took ${d.toMillis} ms") {
        for (cmd <- commands; actor <- actors) {
          actor ! Cmd("pa", cmd)
        }
        for (_ <- actors) {
          testProbe.expectMsg(awaitDurationMillis.millis, commands.last)
        }
        for (actor <- actors) {
          actor ! ResetCounter
        }
      }
    }
  }
}

class NestedPartitionsJournalPerfSpec
    extends PostgresJournalPerfSpec("nested-partitions-application.conf", NestedPartitions)

class NestedPartitionsJournalPerfSpecSharedDb
    extends PostgresJournalPerfSpec("nested-partitions-shared-db-application.conf", NestedPartitions)

class NestedPartitionsJournalPerfSpecPhysicalDelete
    extends PostgresJournalPerfSpec("nested-partitions-application-with-hard-delete.conf", NestedPartitions)

class NestedPartitionsJournalPerfSpecUseJournalMetadata
    extends PostgresJournalPerfSpec("nested-partitions-application-with-use-journal-metadata.conf", NestedPartitions)

class PartitionedJournalPerfSpec extends PostgresJournalPerfSpec("partitioned-application.conf", Partitioned)

class PartitionedJournalPerfSpecSharedDb
    extends PostgresJournalPerfSpec("partitioned-shared-db-application.conf", Partitioned)

class PartitionedJournalPerfSpecPhysicalDelete
    extends PostgresJournalPerfSpec("partitioned-application-with-hard-delete.conf", Partitioned)

class PartitionedJournalPerfSpecUseJournalMetadata
    extends PostgresJournalPerfSpec("partitioned-application-with-use-journal-metadata.conf", Partitioned)

class PlainJournalPerfSpec extends PostgresJournalPerfSpec("plain-application.conf", Plain)

class PlainJournalPerfSpecSharedDb extends PostgresJournalPerfSpec("plain-shared-db-application.conf", Plain)

class PlainJournalPerfSpecPhysicalDelete
    extends PostgresJournalPerfSpec("plain-application-with-hard-delete.conf", Plain)

class PlainJournalPerfSpecUseJournalMetadata
    extends PostgresJournalPerfSpec("plain-application-with-use-journal-metadata.conf", Plain)
