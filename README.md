![Pekko Persistence Postgres](./docs/assets/project-logo.png)

[![License](https://img.shields.io/:license-Apache%202-red.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Actions Status](https://github.com/SwissBorg/pekko-persistence-postgres/workflows/Scala%20CI/badge.svg)](https://github.com/SwissBorg/pekko-persistence-postgres/actions)

> [!WARNING]  
> This Pekko fork is a work in progress.

The Pekko Persistence Postgres plugin allows for using [PostgreSQL 11](https://www.postgresql.org/) and [Amazon Aurora](https://aws.amazon.com/rds/aurora/) databases as backend for Pekko Persistence and Pekko Persistence Query.

It’s been originally created as a fork of [Akka Persistence JDBC plugin](https://github.com/akka/akka-persistence-jdbc), focused on PostgreSQL features such as partitions, arrays, BRIN indexes and others.

The main goal is to keep index size and memory consumption on a moderate level while being able to cope with an increasing data volume.

## Use cases
This plugin supports different schema variants for different use-cases: from small and simple apps, 
through the ones with a small, finite number of persistent actors but each with huge and still growing journals,
to the services with an increasing number of unique persistent actors.

You can read more about DAOs and schema variants in [the official documentation](https://swissborg.github.io/pekko-persistence-postgres/features#support-for-partitioned-tables).

## Adding Pekko Persistence Postgres to your project

To use `pekko-persistence-postgres` in your SBT project, add the following to your `build.sbt`:

```scala
libraryDependencies += "com.swissborg" %% "pekko-persistence-postgres" % "0.1.0"
```

For a maven project add:
```xml
<dependency>
    <groupId>com.swissborg</groupId>
    <artifactId>pekko-persistence-postgres_2.13</artifactId>
    <version>0.1.0</version>
</dependency>
```
to your `pom.xml`.

## Enabling Pekko Persistence Postgres in your project
To use this plugin instead of the default one, add the following to application.conf:
```hocon
pekko.persistence {
  journal.plugin = "postgres-journal"
  snapshot-store.plugin = "postgres-snapshot-store"
}
```
and for persistence query:
```scala
PersistenceQuery(system).readJournalFor[PostgresReadJournal](PostgresReadJournal.Identifier)
```

## Documentation
* [Pekko Persistence Postgres documentation](https://swissborg.github.io/pekko-persistence-postgres/)
* [demo-akka-persistence-postgres](https://github.com/mkubala/demo-akka-persistence-postgres)
> :warning: Please note that this library is based on Pekko, but demo uses an older version of `akka-persistence-postgres` and there might be inconsistencies between the documentation and the provided code


## Key features when compared to the original Pekko Persistence JDBC plugin

### BRIN index on the ordering column
This plugin has been re-designed in terms of handling very large journals.
The original plugin (pekko-persistence-jdbc) uses B-Tree indexes on three columns: `ordering`, `persistence_id` and `sequence_number`. They are great in terms of the query performance and guarding column(s) data uniqueness, but they require relatively a lot of memory.


Wherever it makes sense, we decided to use more lightweight [BRIN indexes](https://www.postgresql.org/docs/11/brin-intro.html).

### Tags as an array of int
Pekko-persistence-jdbc stores all tags in a single column as String separated by an arbitrary separator (by default it’s a comma character).

This solution is quite portable, but not perfect. Queries rely on the `LIKE ‘%tag_name%`’ condition and some additional work needs to be done in order to filter out tags that don't fully match the input `tag_name` (imagine a case when you have the following tags: _healthy_, _unhealthy_ and _neutral_ and want to find all events tagged with _healthy_. The query will return events tagged with both, _healthy_ and _unhealthy_ tags).

Postgres allows columns of a table to be defined as variable-length arrays. 
By mapping event tag names into unique numeric identifiers we could leverage intarray extension, which in some circumstances can improve query performance and reduce query costs up to 10x.

### Support for partitioned tables
When you have big volumes of data and they keep growing, appending events to the journal becomes more expensive - indexes are growing together with tables.

Postgres allows you to split your data between smaller tables (logical partitions) and attach new partitions on demand. Partitioning also applies to indexes, so instead of a one huge B-Tree you can have a number of capped tables with smaller indexes.


You can read more on how Pekko Persistence Postgres leverages partitioning in the _Supported journal schema variants_ section below.

### Minor PostgreSQL optimizations
Beside the aforementioned major changes we did some minor optimizations, like changing the column ordering for [more efficient space utilization](https://www.2ndquadrant.com/en/blog/on-rocks-and-sand/).

## Supported journal schema variants

Currently, plugin supports two variants of the journal table schema:
*flat journal* - a single table, similar to what the JDBC plugin provides. All events are appended to the table. Schema can be found [here](core/src/test/resources/schema/postgres/plain-schema.sql).

This is the default schema.


*journal with nested partitions*  by persistenceId and sequenceNumber - this version allows you to shard your events by the persistenceId. Additionally each of the shards is split by sequenceNumber range to cap the indexes.
You can find the schema [here](core/src/test/resources/schema/postgres/nested-partitions-schema.sql).

This variant is aimed for services that have a finite and/or small number of unique persistence aggregates, but each of them has a big journal.

### Using partitioned journal

In order to start using partitioned journal, you have to create either a partitioned table (here is [the schema](core/src/test/resources/schema/postgres/nested-partitions-schema.sql)) and set the Journal DAO FQCN:
```
postgres-journal.dao = "org.apache.pekko.persistence.postgres.journal.dao.NestedPartitionsJournalDao"
```

The size of the nested partitions (`sequence_number`’s range) can be changed by setting `postgres-journal.tables.journal.partitions.size`. By default partition size is set to `10000000` (10M).

Partitions are automatically created by the plugin in advance. `NestedPartitionsJournalDao` keeps track of created partitions and once sequence_number is out of the range for any known partitions, a new one is created.

Partitions follow the `prefix_sanitizedPersistenceId_partitionNumber` naming pattern.
The `prefix` can be configured by changing the `posgres-journal.tables.journal.partitions.prefix` value. By default it’s set to `j`.
`sanitizedPersistenceId` is PersistenceId with all non-word characters replaced by `_`.
`partitionNumber` is the ordinal number of the partition for a given partition id.

Example partition names: `j_myActor_0`, `j_myActor_1`, `j_worker_0` etc.

Keep in mind that the default maximum length for a table name in Postgres is 63 bytes, so you should avoid any non-ascii characters in your `persistenceId`s and keep the `prefix` reasonably short.

> :warning: Once any of the partitioning setting under  `postgres-journal.tables.journal.partitions` branch is settled, you should never change it.  Otherwise you might end up with PostgresExceptions caused by table name or range conflicts.

## Migration

Please see the documentation regarding migrations [here](https://swissborg.github.io/pekko-persistence-postgres/migration).

## Contributing
We are also always looking for contributions and new ideas, so if you’d like to join the project, check out the [open issues](https://github.com/SwissBorg/pekko-persistence-postgres/issues), or post your own suggestions!

## Sponsors

Development and maintenance of pekko-persistence-postgres is sponsored by:

![SoftwareMill](https://raw.githubusercontent.com/SwissBorg/pekko-persistence-postgres/master/docs/assets/softwaremill-logo.png)

[SoftwareMill](https://softwaremill.com) is a software development and consulting company. We help clients scale their business through software. Our areas of expertise include backends, distributed systems, blockchain, machine learning and data analytics.

![SwissBorg](https://raw.githubusercontent.com/SwissBorg/pekko-persistence-postgres/master/docs/assets/swissborg-logo.png)

[SwissBorg](https://swissborg.com) makes managing your crypto investment easy and helps control your wealth.
