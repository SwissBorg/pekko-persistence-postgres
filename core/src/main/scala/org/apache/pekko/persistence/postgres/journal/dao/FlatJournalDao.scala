package org.apache.pekko.persistence.postgres
package journal.dao

import org.apache.pekko.persistence.postgres.config.JournalConfig
import org.apache.pekko.persistence.postgres.tag.{CachedTagIdResolver, SimpleTagDao}
import org.apache.pekko.serialization.Serialization
import org.apache.pekko.stream.Materializer
import slick.jdbc.JdbcBackend._

import scala.concurrent.ExecutionContext

class FlatJournalDao(val db: Database, val journalConfig: JournalConfig, serialization: Serialization)(implicit
    val ec: ExecutionContext,
    val mat: Materializer
) extends BaseByteArrayJournalDao {
  val queries = new JournalQueries(FlatJournalTable(journalConfig.journalTableConfiguration))
  val tagDao = new SimpleTagDao(db, journalConfig.tagsTableConfiguration)
  val eventTagConverter = new CachedTagIdResolver(tagDao, journalConfig.tagsConfig)
  val serializer = new ByteArrayJournalSerializer(serialization, eventTagConverter)
}
