/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.snapshot.dao

import io.circe.Json
import org.apache.pekko.persistence.postgres.config.SnapshotTableConfiguration
import org.apache.pekko.persistence.postgres.snapshot.dao.SnapshotTables.*

object SnapshotTables {
  case class SnapshotRow(
      persistenceId: String,
      sequenceNumber: Long,
      created: Long,
      snapshot: Array[Byte],
      metadata: Json
  )
}

trait SnapshotTables {
  import org.apache.pekko.persistence.postgres.db.ExtendedPostgresProfile.api.*

  def snapshotTableCfg: SnapshotTableConfiguration

  class Snapshot(_tableTag: Tag)
      extends Table[SnapshotRow](
        _tableTag,
        _schemaName = snapshotTableCfg.schemaName,
        _tableName = snapshotTableCfg.tableName
      ) {
    def * = (
      persistenceId,
      sequenceNumber,
      created,
      snapshot,
      metadata
    ) <> ((SnapshotRow.apply _).tupled, SnapshotRow.unapply)

    val persistenceId: Rep[String] =
      column[String](snapshotTableCfg.columnNames.persistenceId, O.Length(255, varying = true))
    val sequenceNumber: Rep[Long] = column[Long](snapshotTableCfg.columnNames.sequenceNumber)
    val created: Rep[Long] = column[Long](snapshotTableCfg.columnNames.created)
    val snapshot: Rep[Array[Byte]] = column[Array[Byte]](snapshotTableCfg.columnNames.snapshot)
    val metadata: Rep[Json] = column[Json](snapshotTableCfg.columnNames.metadata)
    val pk = primaryKey(s"${tableName}_pk", (persistenceId, sequenceNumber))
  }

  lazy val SnapshotTable = new TableQuery(tag => new Snapshot(tag))
}
