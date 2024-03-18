/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.serialization

import org.apache.pekko.NotUsed
import org.apache.pekko.persistence.{AtomicWrite, PersistentRepr}
import org.apache.pekko.persistence.journal.Tagged
import org.apache.pekko.stream.scaladsl.Flow

import scala.collection.immutable.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait PersistentReprSerializer[T] {

  implicit def executionContext: ExecutionContext

  /** An org.apache.pekko.persistence.AtomicWrite contains a Sequence of events (with metadata, the PersistentRepr) that
    * must all be persisted or all fail, what makes the operation atomic. The function converts each AtomicWrite to a
    * Future[Seq[T]]. The Try denotes whether there was a problem with the AtomicWrite or not.
    */
  def serialize(messages: Seq[AtomicWrite]): Seq[Future[Seq[T]]] = {
    messages.map { atomicWrite =>
      val serialized = atomicWrite.payload.map(serialize)
      Future.sequence(serialized)
    }
  }

  def serialize(persistentRepr: PersistentRepr): Future[T] = persistentRepr.payload match {
    case Tagged(payload, tags) =>
      serialize(persistentRepr.withPayload(payload), tags)
    case _ => serialize(persistentRepr, Set.empty[String])
  }

  def serialize(persistentRepr: PersistentRepr, tags: Set[String]): Future[T]

  /** deserialize into a PersistentRepr, a set of tags and a Long representing the global ordering of events
    */
  def deserialize(t: T): Try[(PersistentRepr, Long)]
}

trait FlowPersistentReprSerializer[T] extends PersistentReprSerializer[T] {

  /** A flow which deserializes each element into a PersistentRepr, a set of tags and a Long representing the global
    * ordering of events
    */
  def deserializeFlow: Flow[T, Try[(PersistentRepr, Long)], NotUsed] = {
    Flow[T].map(deserialize)
  }

}
