/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.serialization

import org.apache.pekko.actor.{ ActorRef, Props }
import org.apache.pekko.event.LoggingReceive
import org.apache.pekko.persistence.postgres.SharedActorSystemTestSpec
import org.apache.pekko.persistence.postgres.util.Schema._
import org.apache.pekko.persistence.{ PersistentActor, RecoveryCompleted }
import org.apache.pekko.testkit.TestProbe

import scala.concurrent.duration._

abstract class StoreOnlySerializableMessagesTest(schemaType: SchemaType)
    extends SharedActorSystemTestSpec(schemaType.configName) {
  case class PersistFailure(cause: Throwable, event: Any, seqNr: Long)
  case class PersistRejected(cause: Throwable, event: Any, seqNr: Long)

  class TestActor(
      val persistenceId: String,
      recoverProbe: ActorRef,
      persistFailureProbe: ActorRef,
      persistRejectedProbe: ActorRef)
      extends PersistentActor {
    override val receiveRecover: Receive = LoggingReceive { case msg =>
      recoverProbe ! msg
    }

    override val receiveCommand: Receive = LoggingReceive { case msg =>
      persist(msg) { _ =>
        sender() ! org.apache.pekko.actor.Status.Success("")
      }
    }

    override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit =
      persistFailureProbe ! PersistFailure(cause, event, seqNr)

    override protected def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit =
      persistRejectedProbe ! PersistRejected(cause, event, seqNr)
  }

  def withActor(id: String = "1")(f: ActorRef => TestProbe => TestProbe => TestProbe => Unit): Unit = {
    val recoverProbe = TestProbe()
    val persistFailureProbe = TestProbe()
    val persistRejectedProbe = TestProbe()
    val persistentActor = system.actorOf(
      Props(new TestActor(s"my-$id", recoverProbe.ref, persistFailureProbe.ref, persistRejectedProbe.ref)))
    try f(persistentActor)(recoverProbe)(persistFailureProbe)(persistRejectedProbe)
    finally killActors(persistentActor)
  }

  override def beforeAll(): Unit = {
    dropCreate(schemaType)
    super.beforeAll()
  }

  it should "persist a single serializable message" in {
    withActor("1") { actor => recover => failure => rejected =>
      val tp = TestProbe()
      recover.expectMsg(RecoveryCompleted)
      tp.send(actor, "foo") // strings are serializable
      tp.expectMsg(org.apache.pekko.actor.Status.Success(""))
      failure.expectNoMessage(100.millis)
      rejected.expectNoMessage(100.millis)
    }

    // the recover cycle
    withActor("1") { actor => recover => failure => rejected =>
      recover.expectMsg("foo")
      recover.expectMsg(RecoveryCompleted)
      failure.expectNoMessage(100.millis)
      rejected.expectNoMessage(100.millis)
    }
  }

  it should "not persist a single non-serializable message" in {
    class NotSerializable
    withActor("2") { actor => recover => failure => rejected =>
      val tp = TestProbe()
      recover.expectMsg(RecoveryCompleted)
      tp.send(actor, new NotSerializable) // the NotSerializable class cannot be serialized
      tp.expectNoMessage(300.millis) // the handler should not have been called, because persist has failed
      // the actor should call the OnPersistRejected
      rejected.expectMsgPF() { case PersistRejected(_, _, _) =>
      }
    }

    // the recover cycle, no message should be recovered
    withActor("2") { actor => recover => failure => rejected =>
      recover.expectMsg(RecoveryCompleted)
      recover.expectNoMessage(100.millis)
    }
  }

  it should "persist only serializable messages" in {
    class NotSerializable
    withActor("3") { actor => recover => failure => rejected =>
      val tp = TestProbe()
      recover.expectMsg(RecoveryCompleted)
      tp.send(actor, "foo")
      tp.expectMsg(org.apache.pekko.actor.Status.Success(""))
      tp.send(actor, new NotSerializable) // the NotSerializable class cannot be serialized
      tp.expectNoMessage(300.millis) // the handler should not have been called, because persist has failed
      // the actor should call the OnPersistRejected
      rejected.expectMsgPF() { case PersistRejected(_, _, _) =>
      }
      rejected.expectNoMessage(100.millis)
    }

    // recover cycle
    withActor("3") { actor => recover => failure => rejected =>
      recover.expectMsg("foo")
      recover.expectMsg(RecoveryCompleted)
      failure.expectNoMessage(100.millis)
      rejected.expectNoMessage(100.millis)
    }
  }
}

class NestedPartitionsStoreOnlySerializableMessagesTest extends StoreOnlySerializableMessagesTest(NestedPartitions)

class PartitionedStoreOnlySerializableMessagesTest extends StoreOnlySerializableMessagesTest(Partitioned)

class PlainStoreOnlySerializableMessagesTest extends StoreOnlySerializableMessagesTest(Plain)
