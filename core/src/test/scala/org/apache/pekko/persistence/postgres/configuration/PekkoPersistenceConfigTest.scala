/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.configuration

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.pekko.persistence.postgres.config.*
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.*

class PekkoPersistenceConfigTest extends AnyFlatSpec with Matchers with OptionValues {
  val config: Config = ConfigFactory.parseString("""
      |postgres-journal {
      |  class = "org.apache.pekko.persistence.postgres.journal.PostgresAsyncWriteJournal"
      |
      |  tables {
      |    journal {
      |      partitions {
      |        size = 12345
      |        prefix = "c"
      |      }
      |      tableName = "journal"
      |      schemaName = ""
      |      columnNames {
      |        ordering = "ordering"
      |        persistenceId = "persistence_id"
      |        sequenceNumber = "sequence_number"
      |        deleted = "deleted"
      |        tags = "tags"
      |        message = "message"
      |      }
      |    }
      |
      |    tags {
      |      tableName = "tag_id_mappings"
      |      schemaName = "staging"
      |      columnNames {
      |        id = "tagId"
      |        name = "label"
      |      }
      |    }
      |  }
      |
      |  dao = "org.apache.pekko.persistence.postgres.journal.dao.FlatJournalDao"
      |
      |  logicalDelete = true
      |
      |  tags {
      |    cacheTtl = 30 minutes
      |    insertionRetryAttempts = 3
      |  }
      |
      |  slick {
      |    profile = "slick.jdbc.PostgresProfile$"
      |    db {
      |      host = "localhost"
      |      host = ${?POSTGRES_HOST}
      |      port = "5432"
      |      port = ${?POSTGRES_PORT}
      |      name = "docker"
      |
      |      url = "jdbc:postgresql://"${pekko-persistence-postgres.slick.db.host}":"${pekko-persistence-postgres.slick.db.port}"/"${pekko-persistence-postgres.slick.db.name}
      |      user = "docker"
      |      password = "docker"
      |      driver = "org.postgresql.Driver$"
      |
      |      // hikariCP settings; see: https://github.com/brettwooldridge/HikariCP
      |
      |      // read: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
      |      // slick will use an async executor with a fixed size queue of 10.000 objects
      |      // The async executor is a connection pool for asynchronous execution of blocking I/O actions.
      |      // This is used for the asynchronous query execution API on top of blocking back-ends like JDBC.
      |      queueSize = 10000 // number of objects that can be queued by the async exector
      |
      |      connectionTimeout = 30000 // This property controls the maximum number of milliseconds that a client (that's you) will wait for a connection from the pool. If this time is exceeded without a connection becoming available, a SQLException will be thrown. 1000ms is the minimum value. Default: 30000 (30 seconds)
      |      validationTimeout = 5000 // This property controls the maximum amount of time that a connection will be tested for aliveness. This value must be less than the connectionTimeout. The lowest accepted validation timeout is 1000ms (1 second). Default: 5000
      |      idleTimeout = 600000 // 10 minutes: This property controls the maximum amount of time that a connection is allowed to sit idle in the pool. Whether a connection is retired as idle or not is subject to a maximum variation of +30 seconds, and average variation of +15 seconds. A connection will never be retired as idle before this timeout. A value of 0 means that idle connections are never removed from the pool. Default: 600000 (10 minutes)
      |      maxLifetime = 1800000 // 30 minutes: This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout it will be retired from the pool, subject to a maximum variation of +30 seconds. An in-use connection will never be retired, only when it is closed will it then be removed. We strongly recommend setting this value, and it should be at least 30 seconds less than any database-level connection timeout. A value of 0 indicates no maximum lifetime (infinite lifetime), subject of course to the idleTimeout setting. Default: 1800000 (30 minutes)
      |      leakDetectionThreshold = 0 // This property controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak. A value of 0 means leak detection is disabled. Lowest acceptable value for enabling leak detection is 2000 (2 secs). Default: 0
      |
      |      initializationFailFast = true // This property controls whether the pool will "fail fast" if the pool cannot be seeded with initial connections successfully. If you want your application to start even when the database is down/unavailable, set this property to false. Default: true
      |
      |      keepAliveConnection = on // ensures that the database does not get dropped while we are using it
      |
      |      numThreads = 4 // number of cores
      |      maxConnections = 4  // same as numThreads
      |      minConnections = 4  // same as numThreads
      |    }
      |  }
      |}
      |
      |# the pekko-persistence-snapshot-store in use
      |postgres-snapshot-store {
      |  class = "org.apache.pekko.persistence.postgres.snapshot.PostgresSnapshotStore"
      |
      |  tables {
      |    snapshot {
      |      tableName = "snapshot"
      |      schemaName = ""
      |      columnNames {
      |        persistenceId = "persistence_id"
      |        sequenceNumber = "sequence_number"
      |        created = "created"
      |        snapshot = "snapshot"
      |      }
      |    }
      |  }
      |
      |  dao = "org.apache.pekko.persistence.postgres.snapshot.dao.ByteArraySnapshotDao"
      |
      |  slick {
      |    profile = "slick.jdbc.MySQLProfile$"
      |    db {
      |      host = "localhost"
      |      host = ${?POSTGRES_HOST}
      |      port = "5432"
      |      port = ${?POSTGRES_PORT}
      |      name = "docker"
      |
      |      url = "jdbc:postgresql://"${pekko-persistence-postgres.slick.db.host}":"${pekko-persistence-postgres.slick.db.port}"/"${pekko-persistence-postgres.slick.db.name}
      |      user = "docker"
      |      password = "docker"
      |      driver = "org.postgresql.Driver"
      |
      |      // hikariCP settings; see: https://github.com/brettwooldridge/HikariCP
      |
      |      // read: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
      |      // slick will use an async executor with a fixed size queue of 10.000 objects
      |      // The async executor is a connection pool for asynchronous execution of blocking I/O actions.
      |      // This is used for the asynchronous query execution API on top of blocking back-ends like JDBC.
      |      queueSize = 10000 // number of objects that can be queued by the async exector
      |
      |      connectionTimeout = 30000 // This property controls the maximum number of milliseconds that a client (that's you) will wait for a connection from the pool. If this time is exceeded without a connection becoming available, a SQLException will be thrown. 1000ms is the minimum value. Default: 30000 (30 seconds)
      |      validationTimeout = 5000 // This property controls the maximum amount of time that a connection will be tested for aliveness. This value must be less than the connectionTimeout. The lowest accepted validation timeout is 1000ms (1 second). Default: 5000
      |      idleTimeout = 600000 // 10 minutes: This property controls the maximum amount of time that a connection is allowed to sit idle in the pool. Whether a connection is retired as idle or not is subject to a maximum variation of +30 seconds, and average variation of +15 seconds. A connection will never be retired as idle before this timeout. A value of 0 means that idle connections are never removed from the pool. Default: 600000 (10 minutes)
      |      maxLifetime = 1800000 // 30 minutes: This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout it will be retired from the pool, subject to a maximum variation of +30 seconds. An in-use connection will never be retired, only when it is closed will it then be removed. We strongly recommend setting this value, and it should be at least 30 seconds less than any database-level connection timeout. A value of 0 indicates no maximum lifetime (infinite lifetime), subject of course to the idleTimeout setting. Default: 1800000 (30 minutes)
      |      leakDetectionThreshold = 0 // This property controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak. A value of 0 means leak detection is disabled. Lowest acceptable value for enabling leak detection is 2000 (2 secs). Default: 0
      |
      |      initializationFailFast = true // This property controls whether the pool will "fail fast" if the pool cannot be seeded with initial connections successfully. If you want your application to start even when the database is down/unavailable, set this property to false. Default: true
      |
      |      keepAliveConnection = on // ensures that the database does not get dropped while we are using it
      |
      |      numThreads = 4 // number of cores
      |      maxConnections = 4  // same as numThreads
      |      minConnections = 4  // same as numThreads
      |    }
      |  }
      |}
      |
      |# the pekko-persistence-query provider in use
      |postgres-read-journal {
      |  class = "org.apache.pekko.persistence.postgres.query.PostgresReadJournalProvider"
      |
      |  # New events are retrieved (polled) with this interval.
      |  refresh-interval = "300ms"
      |
      |  # How many events to fetch in one query (replay) and keep buffered until they
      |  # are delivered downstreams.
      |  max-buffer-size = "10"
      |
      |  dao = "org.apache.pekko.persistence.postgres.query.dao.FlatReadJournalDao"
      |
      |  tags {
      |    cacheTtl = 12 hours
      |  }
      |
      |  tables {
      |    journal {
      |      tableName = "journal"
      |      schemaName = ""
      |      columnNames {
      |        ordering = "ordering"
      |        persistenceId = "persistence_id"
      |        sequenceNumber = "sequence_number"
      |        created = "created"
      |        tags = "tags"
      |        message = "message"
      |      }
      |    }
      |    tags {
      |      tableName = "tag_id_mappings"
      |      schemaName = "staging"
      |      columnNames {
      |        id = "tagId"
      |        name = "label"
      |      }
      |    }
      |  }
      |
      |  slick {
      |    profile = "slick.jdbc.OracleProfile$"
      |    db {
      |      host = "localhost"
      |      host = ${?POSTGRES_HOST}
      |      port = "5432"
      |      port = ${?POSTGRES_PORT}
      |      name = "docker"
      |
      |      url = "jdbc:postgresql://"${pekko-persistence-postgres.slick.db.host}":"${pekko-persistence-postgres.slick.db.port}"/"${pekko-persistence-postgres.slick.db.name}
      |      user = "docker"
      |      password = "docker"
      |      driver = "org.postgresql.Driver"
      |
      |      // hikariCP settings; see: https://github.com/brettwooldridge/HikariCP
      |
      |      // read: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
      |      // slick will use an async executor with a fixed size queue of 10.000 objects
      |      // The async executor is a connection pool for asynchronous execution of blocking I/O actions.
      |      // This is used for the asynchronous query execution API on top of blocking back-ends like JDBC.
      |      queueSize = 10000 // number of objects that can be queued by the async exector
      |
      |      connectionTimeout = 30000 // This property controls the maximum number of milliseconds that a client (that's you) will wait for a connection from the pool. If this time is exceeded without a connection becoming available, a SQLException will be thrown. 1000ms is the minimum value. Default: 30000 (30 seconds)
      |      validationTimeout = 5000 // This property controls the maximum amount of time that a connection will be tested for aliveness. This value must be less than the connectionTimeout. The lowest accepted validation timeout is 1000ms (1 second). Default: 5000
      |      idleTimeout = 600000 // 10 minutes: This property controls the maximum amount of time that a connection is allowed to sit idle in the pool. Whether a connection is retired as idle or not is subject to a maximum variation of +30 seconds, and average variation of +15 seconds. A connection will never be retired as idle before this timeout. A value of 0 means that idle connections are never removed from the pool. Default: 600000 (10 minutes)
      |      maxLifetime = 1800000 // 30 minutes: This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout it will be retired from the pool, subject to a maximum variation of +30 seconds. An in-use connection will never be retired, only when it is closed will it then be removed. We strongly recommend setting this value, and it should be at least 30 seconds less than any database-level connection timeout. A value of 0 indicates no maximum lifetime (infinite lifetime), subject of course to the idleTimeout setting. Default: 1800000 (30 minutes)
      |      leakDetectionThreshold = 0 // This property controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak. A value of 0 means leak detection is disabled. Lowest acceptable value for enabling leak detection is 2000 (2 secs). Default: 0
      |
      |      initializationFailFast = true // This property controls whether the pool will "fail fast" if the pool cannot be seeded with initial connections successfully. If you want your application to start even when the database is down/unavailable, set this property to false. Default: true
      |
      |      keepAliveConnection = on // ensures that the database does not get dropped while we are using it
      |
      |      numThreads = 4 // number of cores
      |      maxConnections = 4  // same as numThreads
      |      minConnections = 4  // same as numThreads
      |    }
      |  }
      |}
    """.stripMargin)

  "empty config" should "parse JournalConfig" in {
    val cfg = new JournalConfig(ConfigFactory.empty)
    val slickConfiguration = new SlickConfiguration(ConfigFactory.empty)
    slickConfiguration.jndiName shouldBe None
    slickConfiguration.jndiDbName shouldBe None

    cfg.pluginConfig.dao shouldBe "org.apache.pekko.persistence.postgres.journal.dao.FlatJournalDao"

    cfg.journalTableConfiguration.tableName shouldBe "journal"
    cfg.journalTableConfiguration.schemaName shouldBe None

    cfg.journalTableConfiguration.columnNames.ordering shouldBe "ordering"
    cfg.journalTableConfiguration.columnNames.created shouldBe "created"
    cfg.journalTableConfiguration.columnNames.message shouldBe "message"
    cfg.journalTableConfiguration.columnNames.persistenceId shouldBe "persistence_id"
    cfg.journalTableConfiguration.columnNames.sequenceNumber shouldBe "sequence_number"
    cfg.journalTableConfiguration.columnNames.tags shouldBe "tags"

    cfg.daoConfig.logicalDelete shouldBe true

    cfg.tagsConfig.cacheTtl should equal(1.hour)
    cfg.tagsConfig.insertionRetryAttempts shouldBe 1

    cfg.tagsTableConfiguration.tableName shouldBe "tags"
    cfg.tagsTableConfiguration.schemaName should not be defined
    cfg.tagsTableConfiguration.columnNames.id shouldBe "id"
    cfg.tagsTableConfiguration.columnNames.name shouldBe "name"
  }

  it should "parse SnapshotConfig" in {
    val cfg = new SnapshotConfig(ConfigFactory.empty)
    val slickConfiguration = new SlickConfiguration(ConfigFactory.empty)
    slickConfiguration.jndiName shouldBe None
    slickConfiguration.jndiDbName shouldBe None

    cfg.pluginConfig.dao shouldBe "org.apache.pekko.persistence.postgres.snapshot.dao.ByteArraySnapshotDao"

    cfg.snapshotTableConfiguration.tableName shouldBe "snapshot"
    cfg.snapshotTableConfiguration.schemaName shouldBe None

    cfg.snapshotTableConfiguration.columnNames.persistenceId shouldBe "persistence_id"
    cfg.snapshotTableConfiguration.columnNames.created shouldBe "created"
    cfg.snapshotTableConfiguration.columnNames.sequenceNumber shouldBe "sequence_number"
    cfg.snapshotTableConfiguration.columnNames.snapshot shouldBe "snapshot"
  }

  it should "parse ReadJournalConfig" in {
    val cfg = new ReadJournalConfig(ConfigFactory.empty)
    val slickConfiguration = new SlickConfiguration(ConfigFactory.empty)
    slickConfiguration.jndiName shouldBe None
    slickConfiguration.jndiDbName shouldBe None

    cfg.pluginConfig.dao shouldBe "org.apache.pekko.persistence.postgres.query.dao.FlatReadJournalDao"
    cfg.refreshInterval shouldBe 1.second
    cfg.maxBufferSize shouldBe 500

    cfg.journalTableConfiguration.tableName shouldBe "journal"
    cfg.journalTableConfiguration.schemaName shouldBe None

    cfg.journalTableConfiguration.columnNames.ordering shouldBe "ordering"
    cfg.journalTableConfiguration.columnNames.created shouldBe "created"
    cfg.journalTableConfiguration.columnNames.message shouldBe "message"
    cfg.journalTableConfiguration.columnNames.persistenceId shouldBe "persistence_id"
    cfg.journalTableConfiguration.columnNames.sequenceNumber shouldBe "sequence_number"
    cfg.journalTableConfiguration.columnNames.tags shouldBe "tags"

    cfg.tagsConfig.cacheTtl should equal(1.hour)
    cfg.tagsConfig.insertionRetryAttempts shouldBe 1

    cfg.tagsTableConfiguration.tableName shouldBe "tags"
    cfg.tagsTableConfiguration.schemaName should not be defined
    cfg.tagsTableConfiguration.columnNames.id shouldBe "id"
    cfg.tagsTableConfiguration.columnNames.name shouldBe "name"
  }

  "full config" should "parse JournalConfig" in {
    val cfg = new JournalConfig(config.getConfig("postgres-journal"))
    val slickConfiguration = new SlickConfiguration(config.getConfig("postgres-journal.slick"))
    slickConfiguration.jndiName shouldBe None
    slickConfiguration.jndiDbName shouldBe None

    cfg.pluginConfig.dao shouldBe "org.apache.pekko.persistence.postgres.journal.dao.FlatJournalDao"

    cfg.journalTableConfiguration.tableName shouldBe "journal"
    cfg.journalTableConfiguration.schemaName shouldBe None

    cfg.journalTableConfiguration.columnNames.ordering shouldBe "ordering"
    cfg.journalTableConfiguration.columnNames.created shouldBe "created"
    cfg.journalTableConfiguration.columnNames.message shouldBe "message"
    cfg.journalTableConfiguration.columnNames.persistenceId shouldBe "persistence_id"
    cfg.journalTableConfiguration.columnNames.sequenceNumber shouldBe "sequence_number"
    cfg.journalTableConfiguration.columnNames.tags shouldBe "tags"

    cfg.tagsConfig.cacheTtl should equal(30.minutes)
    cfg.tagsConfig.insertionRetryAttempts shouldBe 3

    cfg.tagsTableConfiguration.tableName shouldBe "tag_id_mappings"
    cfg.tagsTableConfiguration.schemaName.value shouldBe "staging"
    cfg.tagsTableConfiguration.columnNames.id shouldBe "tagId"
    cfg.tagsTableConfiguration.columnNames.name shouldBe "label"

    cfg.partitionsConfig.prefix shouldBe "c"
    cfg.partitionsConfig.size shouldBe 12345
  }

  it should "parse SnapshotConfig" in {
    val cfg = new SnapshotConfig(config.getConfig("postgres-snapshot-store"))
    val slickConfiguration = new SlickConfiguration(config.getConfig("postgres-snapshot-store.slick"))
    slickConfiguration.jndiName shouldBe None
    slickConfiguration.jndiDbName shouldBe None

    cfg.pluginConfig.dao shouldBe "org.apache.pekko.persistence.postgres.snapshot.dao.ByteArraySnapshotDao"

    cfg.snapshotTableConfiguration.tableName shouldBe "snapshot"
    cfg.snapshotTableConfiguration.schemaName shouldBe None

    cfg.snapshotTableConfiguration.columnNames.persistenceId shouldBe "persistence_id"
    cfg.snapshotTableConfiguration.columnNames.created shouldBe "created"
    cfg.snapshotTableConfiguration.columnNames.sequenceNumber shouldBe "sequence_number"
    cfg.snapshotTableConfiguration.columnNames.snapshot shouldBe "snapshot"
  }

  it should "parse ReadJournalConfig" in {
    val cfg = new ReadJournalConfig(config.getConfig("postgres-read-journal"))
    val slickConfiguration = new SlickConfiguration(config.getConfig("postgres-read-journal.slick"))
    slickConfiguration.jndiName shouldBe None
    slickConfiguration.jndiDbName shouldBe None

    cfg.pluginConfig.dao shouldBe "org.apache.pekko.persistence.postgres.query.dao.FlatReadJournalDao"
    cfg.refreshInterval shouldBe 300.millis
    cfg.maxBufferSize shouldBe 10

    cfg.journalTableConfiguration.tableName shouldBe "journal"
    cfg.journalTableConfiguration.schemaName shouldBe None

    cfg.journalTableConfiguration.columnNames.ordering shouldBe "ordering"
    cfg.journalTableConfiguration.columnNames.created shouldBe "created"
    cfg.journalTableConfiguration.columnNames.message shouldBe "message"
    cfg.journalTableConfiguration.columnNames.persistenceId shouldBe "persistence_id"
    cfg.journalTableConfiguration.columnNames.sequenceNumber shouldBe "sequence_number"
    cfg.journalTableConfiguration.columnNames.tags shouldBe "tags"

    cfg.tagsConfig.cacheTtl should equal(12.hours)
    cfg.tagsConfig.insertionRetryAttempts shouldBe 1

    cfg.tagsTableConfiguration.tableName shouldBe "tag_id_mappings"
    cfg.tagsTableConfiguration.schemaName.value shouldBe "staging"
    cfg.tagsTableConfiguration.columnNames.id shouldBe "tagId"
    cfg.tagsTableConfiguration.columnNames.name shouldBe "label"
  }
}
