package org.apache.pekko.persistence.postgres.query.dao

import org.apache.pekko.persistence.postgres.journal.dao.JournalMetadataTable
import org.apache.pekko.persistence.postgres.util.BaseQueryTest

class ReadJournalMetadataQueriesTest extends BaseQueryTest {
  it should "create SQL query for minAndMaxOrderingForPersistenceId" in withReadJournalMetadataQueries { queries =>
    queries.minAndMaxOrderingForPersistenceId(
      "aaa") shouldBeSQL """select "min_ordering", "max_ordering" from "journal_metadata" where "persistence_id" = ? limit 1"""
  }

  private def withReadJournalMetadataQueries(f: ReadJournalMetadataQueries => Unit): Unit = {
    withActorSystem { implicit system =>
      f(new ReadJournalMetadataQueries(JournalMetadataTable(readJournalConfig.journalMetadataTableConfiguration)))
    }
  }
}
