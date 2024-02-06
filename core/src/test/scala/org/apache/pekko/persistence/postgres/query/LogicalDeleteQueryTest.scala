/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.query

import org.apache.pekko.pattern._
import org.apache.pekko.persistence.postgres.util.Schema.{ NestedPartitions, Partitioned, Plain, SchemaType }
import org.apache.pekko.persistence.query.{ EventEnvelope, NoOffset, Sequence }

import scala.concurrent.duration._

abstract class LogicalDeleteQueryTest(val schemaType: SchemaType) extends QueryTestSpec(schemaType.configName) {
  implicit val askTimeout: FiniteDuration = 500.millis

  it should "return logically deleted events when using CurrentEventsByTag (backward compatibility)" in withActorSystem {
    implicit system =>
      val journalOps = new ScalaPostgresReadJournalOperations(system)
      withTestActors(replyToMessages = true) { (actor1, _, _) =>
        (actor1 ? withTags(1, "number")).futureValue
        (actor1 ? withTags(2, "number")).futureValue
        (actor1 ? withTags(3, "number")).futureValue

        // delete and wait for confirmation
        (actor1 ? DeleteCmd(1)).futureValue

        journalOps.withCurrentEventsByTag()("number", NoOffset) { tp =>
          tp.request(Int.MaxValue)
          tp.expectNextPF { case EventEnvelope(Sequence(1), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(2), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(3), _, _, _) => }
          tp.expectComplete()
        }
      }
  }

  it should "return logically deleted events when using EventsByTag (backward compatibility)" in withActorSystem {
    implicit system =>
      val journalOps = new ScalaPostgresReadJournalOperations(system)
      withTestActors(replyToMessages = true) { (actor1, _, _) =>
        (actor1 ? withTags(1, "number")).futureValue
        (actor1 ? withTags(2, "number")).futureValue
        (actor1 ? withTags(3, "number")).futureValue

        // delete and wait for confirmation
        (actor1 ? DeleteCmd(1)).futureValue shouldBe "deleted-1"

        journalOps.withEventsByTag()("number", NoOffset) { tp =>
          tp.request(Int.MaxValue)
          tp.expectNextPF { case EventEnvelope(Sequence(1), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(2), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(3), _, _, _) => }
          tp.cancel()
        }
      }
  }

  it should "return logically deleted events when using CurrentEventsByPersistenceId (backward compatibility)" in withActorSystem {
    implicit system =>
      val journalOps = new ScalaPostgresReadJournalOperations(system)
      withTestActors(replyToMessages = true) { (actor1, _, _) =>
        (actor1 ? withTags(1, "number")).futureValue
        (actor1 ? withTags(2, "number")).futureValue
        (actor1 ? withTags(3, "number")).futureValue

        // delete and wait for confirmation
        (actor1 ? DeleteCmd(1)).futureValue shouldBe "deleted-1"

        journalOps.withCurrentEventsByPersistenceId()("my-1") { tp =>
          tp.request(Int.MaxValue)
          tp.expectNextPF { case EventEnvelope(Sequence(1), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(2), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(3), _, _, _) => }
          tp.expectComplete()
        }
      }
  }

  it should "return logically deleted events when using EventsByPersistenceId (backward compatibility)" in withActorSystem {
    implicit system =>
      val journalOps = new ScalaPostgresReadJournalOperations(system)
      withTestActors(replyToMessages = true) { (actor1, _, _) =>
        (actor1 ? withTags(1, "number")).futureValue
        (actor1 ? withTags(2, "number")).futureValue
        (actor1 ? withTags(3, "number")).futureValue

        // delete and wait for confirmation
        (actor1 ? DeleteCmd(1)).futureValue shouldBe "deleted-1"

        journalOps.withEventsByPersistenceId()("my-1") { tp =>
          tp.request(Int.MaxValue)
          tp.expectNextPF { case EventEnvelope(Sequence(1), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(2), _, _, _) => }
          tp.expectNextPF { case EventEnvelope(Sequence(3), _, _, _) => }
          tp.cancel()
        }
      }
  }
}

class NestedPartitionsLogicalDeleteQueryTest extends LogicalDeleteQueryTest(NestedPartitions)

class PartitionedLogicalDeleteQueryTest extends LogicalDeleteQueryTest(Partitioned)

class PlainLogicalDeleteQueryTest extends LogicalDeleteQueryTest(Plain)
