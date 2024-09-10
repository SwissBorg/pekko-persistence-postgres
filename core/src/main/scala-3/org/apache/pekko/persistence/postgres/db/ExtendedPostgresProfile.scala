package org.apache.pekko.persistence.postgres.db

import com.github.tminglei.slickpg.*
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait ExtendedPostgresProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgNetSupport
    with PgLTreeSupport
    with array.PgArrayJdbcTypes
    with PgCirceJsonSupport {

  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api: MyAPI.type = MyAPI

  trait MyAPI
      extends ExtPostgresAPI
      with ArrayImplicits
      with SimpleArrayPlainImplicits
      with Date2DateTimeImplicitsDuration
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants
      with JsonImplicits {
    implicit val strListTypeMapper: DriverJdbcType[List[String]] = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }
  object MyAPI extends MyAPI
}

object ExtendedPostgresProfile extends ExtendedPostgresProfile
