package org.apache.pekko.persistence.postgres.query.dao

import org.apache.pekko.persistence.postgres.journal.dao.JournalMetadataTable
import slick.lifted.TableQuery

class ReadJournalMetadataQueries(journalMetadataTable: TableQuery[JournalMetadataTable]) {

  import org.apache.pekko.persistence.postgres.db.ExtendedPostgresProfile.api.*

  private def _minAndMaxOrderingForPersistenceId(
      persistenceId: Rep[String]
  ): Query[(Rep[Long], Rep[Long]), (Long, Long), Seq] =
    journalMetadataTable.filter(_.persistenceId === persistenceId).take(1).map(r => (r.minOrdering, r.maxOrdering))

  val minAndMaxOrderingForPersistenceId = Compiled(_minAndMaxOrderingForPersistenceId _)
}
