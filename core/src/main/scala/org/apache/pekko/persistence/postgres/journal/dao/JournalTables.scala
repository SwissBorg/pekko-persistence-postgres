/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres
package journal.dao

import org.apache.pekko.persistence.postgres.config.{ JournalMetadataTableConfiguration, JournalTableConfiguration }
import org.apache.pekko.persistence.postgres.db.ExtendedPostgresProfile.api._
import io.circe.Json

trait JournalTable extends Table[JournalRow] {
  def ordering: Rep[Long]
  def persistenceId: Rep[String]
  def sequenceNumber: Rep[Long]
  def deleted: Rep[Boolean]
  def tags: Rep[List[Int]]
  def message: Rep[Array[Byte]]
  def metadata: Rep[Json]
}

abstract class BaseJournalTable(_tableTag: Tag, journalTableCfg: JournalTableConfiguration)
    extends Table[JournalRow](
      _tableTag,
      _schemaName = journalTableCfg.schemaName,
      _tableName = journalTableCfg.tableName)
    with JournalTable

class FlatJournalTable private[dao] (_tableTag: Tag, journalTableCfg: JournalTableConfiguration)
    extends BaseJournalTable(_tableTag, journalTableCfg) {
  def * = (
    ordering,
    deleted,
    persistenceId,
    sequenceNumber,
    message,
    tags,
    metadata) <> (JournalRow.tupled, JournalRow.unapply)

  val ordering: Rep[Long] = column[Long](journalTableCfg.columnNames.ordering, O.AutoInc)
  val persistenceId: Rep[String] =
    column[String](journalTableCfg.columnNames.persistenceId, O.Length(255, varying = true))
  val sequenceNumber: Rep[Long] = column[Long](journalTableCfg.columnNames.sequenceNumber)
  val deleted: Rep[Boolean] = column[Boolean](journalTableCfg.columnNames.deleted, O.Default(false))
  val tags: Rep[List[Int]] = column[List[Int]](journalTableCfg.columnNames.tags)
  val message: Rep[Array[Byte]] = column[Array[Byte]](journalTableCfg.columnNames.message)
  val metadata: Rep[Json] = column[Json](journalTableCfg.columnNames.metadata)

  val pk = primaryKey(s"${tableName}_pk", (persistenceId, sequenceNumber))
  val orderingIdx = index(s"${tableName}_ordering_idx", ordering, unique = true)
  val tagsIdx = index(s"${tableName}_tags_idx", tags)
}

object FlatJournalTable {
  def apply(journalTableCfg: JournalTableConfiguration): TableQuery[JournalTable] =
    TableQuery(tag => new FlatJournalTable(tag, journalTableCfg))
}

class PartitionedJournalTable private (_tableTag: Tag, journalTableCfg: JournalTableConfiguration)
    extends BaseJournalTable(_tableTag, journalTableCfg) {
  def * = (
    ordering,
    deleted,
    persistenceId,
    sequenceNumber,
    message,
    tags,
    metadata) <> (JournalRow.tupled, JournalRow.unapply)

  val ordering: Rep[Long] = column[Long](journalTableCfg.columnNames.ordering)
  val persistenceId: Rep[String] =
    column[String](journalTableCfg.columnNames.persistenceId, O.Length(255, varying = true))
  val sequenceNumber: Rep[Long] = column[Long](journalTableCfg.columnNames.sequenceNumber)
  val deleted: Rep[Boolean] = column[Boolean](journalTableCfg.columnNames.deleted, O.Default(false))
  val tags: Rep[List[Int]] = column[List[Int]](journalTableCfg.columnNames.tags)
  val message: Rep[Array[Byte]] = column[Array[Byte]](journalTableCfg.columnNames.message)
  val metadata: Rep[Json] = column[Json](journalTableCfg.columnNames.metadata)

  val pk = primaryKey(s"${tableName}_pk", (persistenceId, sequenceNumber, ordering))
  val tagsIdx = index(s"${tableName}_tags_idx", tags)
}

object PartitionedJournalTable {
  def apply(journalTableCfg: JournalTableConfiguration): TableQuery[JournalTable] =
    TableQuery(tag => new PartitionedJournalTable(tag, journalTableCfg))
}

object NestedPartitionsJournalTable {
  def apply(journalTableCfg: JournalTableConfiguration): TableQuery[JournalTable] =
    FlatJournalTable.apply(journalTableCfg)
}

class JournalMetadataTable(_tableTag: Tag, journalMetadataTableCfg: JournalMetadataTableConfiguration)
    extends Table[JournalMetadataRow](
      _tableTag,
      _schemaName = journalMetadataTableCfg.schemaName,
      _tableName = journalMetadataTableCfg.tableName) {
  override def * = (
    id,
    persistenceId,
    maxSequenceNumber,
    minOrdering,
    maxOrdering) <> (JournalMetadataRow.tupled, JournalMetadataRow.unapply)

  val id: Rep[Long] = column[Long](journalMetadataTableCfg.columnNames.id)
  val persistenceId: Rep[String] =
    column[String](journalMetadataTableCfg.columnNames.persistenceId, O.Length(255, varying = true))
  val maxSequenceNumber: Rep[Long] = column[Long](journalMetadataTableCfg.columnNames.maxSequenceNumber)
  val minOrdering: Rep[Long] = column[Long](journalMetadataTableCfg.columnNames.minOrdering)
  val maxOrdering: Rep[Long] = column[Long](journalMetadataTableCfg.columnNames.maxOrdering)

  val pk = primaryKey(s"${tableName}_pk", persistenceId)
}

object JournalMetadataTable {
  def apply(
             journalMetadataTableCfg: JournalMetadataTableConfiguration): TableQuery[JournalMetadataTable] =
    TableQuery(tag => new JournalMetadataTable(tag, journalMetadataTableCfg))
}
