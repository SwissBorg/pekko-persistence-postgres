package org.apache.pekko.persistence.postgres.tag

import org.apache.pekko.persistence.postgres.config.TagsTableConfiguration
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

trait TagDao {

  def find(tagName: String): Future[Option[Int]]

  def insert(tagName: String): Future[Int]
}

class SimpleTagDao(db: Database, tagsTableCfg: TagsTableConfiguration)(implicit ec: ExecutionContext) extends TagDao {
  import org.apache.pekko.persistence.postgres.db.ExtendedPostgresProfile.api.*

  private val queries = new EventTagQueries(tagsTableCfg)

  def find(tagName: String): Future[Option[Int]] =
    db.run(queries.selectByName(tagName).map(_.map(_.id)).result.headOption)

  def insert(tagName: String): Future[Int] =
    db.run(queries.add(EventTag(Int.MinValue, tagName)).transactionally)
}
