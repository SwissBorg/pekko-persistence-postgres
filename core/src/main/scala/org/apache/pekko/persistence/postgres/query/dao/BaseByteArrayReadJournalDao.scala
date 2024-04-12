/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres
package query.dao

import org.apache.pekko.NotUsed
import org.apache.pekko.persistence.PersistentRepr
import org.apache.pekko.persistence.postgres.config.ReadJournalConfig
import org.apache.pekko.persistence.postgres.journal.dao.BaseJournalDaoWithReadMessages
import org.apache.pekko.persistence.postgres.serialization.FlowPersistentReprSerializer
import org.apache.pekko.persistence.postgres.tag.TagIdResolver
import org.apache.pekko.stream.scaladsl.Source
import slick.basic.DatabasePublisher
import slick.jdbc.JdbcBackend.*

import scala.collection.immutable.*
import scala.concurrent.Future
import scala.util.Try

trait BaseByteArrayReadJournalDao extends ReadJournalDao with BaseJournalDaoWithReadMessages {
  def db: Database
  def queries: ReadJournalQueries
  def serializer: FlowPersistentReprSerializer[JournalRow]
  def tagIdResolver: TagIdResolver
  def readJournalConfig: ReadJournalConfig

  import org.apache.pekko.persistence.postgres.db.ExtendedPostgresProfile.api._

  override def allPersistenceIdsSource(max: Long): Source[String, NotUsed] =
    Source.fromPublisher(db.stream(queries.allPersistenceIdsDistinct(max).result))

  override def eventsByTag(
      tag: String,
      offset: Long,
      maxOffset: Long,
      max: Long
  ): Source[Try[(PersistentRepr, Long)], NotUsed] = {
    val publisher: Int => DatabasePublisher[JournalRow] = tagId =>
      db.stream(queries.eventsByTag(List(tagId), offset, maxOffset).result)
    Source
      .future(tagIdResolver.lookupIdFor(tag))
      .flatMapConcat(_.fold(Source.empty[JournalRow])(tagId => Source.fromPublisher(publisher(tagId))))
      .via(serializer.deserializeFlow)
  }

  override def messages(
      persistenceId: String,
      fromSequenceNr: Long,
      toSequenceNr: Long,
      max: Long
  ): Source[Try[(PersistentRepr, Long)], NotUsed] =
    Source
      .fromPublisher(db.stream(queries.messagesQuery(persistenceId, fromSequenceNr, toSequenceNr, max).result))
      .via(serializer.deserializeFlow)

  override def journalSequence(offset: Long, limit: Long): Source[Long, NotUsed] =
    Source.fromPublisher(db.stream(queries.orderingByOrdering(offset, limit).result))

  override def maxJournalSequence(): Future[Long] = {
    db.run(queries.maxOrdering.result)
  }
}
