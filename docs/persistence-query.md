---
layout: page
title: Persistence Query
permalink: /persistence-query
nav_order: 30
---

# Persistence Query
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

### How to get the ReadJournal using Scala

The `ReadJournal` is retrieved via the `org.apache.pekko.persistence.query.PersistenceQuery` extension:

```scala
import org.apache.pekko.persistence.query.PersistenceQuery
import org.apache.pekko.persistence.postgres.query.scaladsl.PostgresReadJournal

val readJournal: PostgresReadJournal = PersistenceQuery(system).readJournalFor[PostgresReadJournal](PostgresReadJournal.Identifier)
```

### How to get the ReadJournal using Java

The `ReadJournal` is retrieved via the `org.apache.pekko.persistence.query.PersistenceQuery` extension:

```java
import org.apache.pekko.persistence.query.PersistenceQuery;
import org.apache.pekko.persistence.postgres.query.javadsl.PostgresReadJournal;

final PostgresReadJournal readJournal = PersistenceQuery.get(system).getReadJournalFor(PostgresReadJournal.class, PostgresReadJournal.Identifier());
```

## Supported persistence queries

The plugin supports the following queries:

### AllPersistenceIdsQuery and CurrentPersistenceIdsQuery

`allPersistenceIds` and `currentPersistenceIds` are used for retrieving all persistenceIds of all persistent actors.

```scala
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, ActorMaterializer}
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.persistence.query.PersistenceQuery
import org.apache.pekko.persistence.postgres.query.scaladsl.PostgresReadJournal

implicit val system: ActorSystem = ActorSystem()
implicit val mat: Materializer = ActorMaterializer()(system)
val readJournal: PostgresReadJournal = PersistenceQuery(system).readJournalFor[PostgresReadJournal](PostgresReadJournal.Identifier)

val willNotCompleteTheStream: Source[String, NotUsed] = readJournal.allPersistenceIds()

val willCompleteTheStream: Source[String, NotUsed] = readJournal.currentPersistenceIds()
```

The returned event stream is unordered and you can expect different order for multiple executions of the query.

When using the `allPersistenceIds` query, the stream is not completed when it reaches the end of the currently used persistenceIds,
but it continues to push new persistenceIds when new persistent actors are created.

When using the `currentPersistenceIds` query, the stream is completed when the end of the current list of persistenceIds is reached,
thus it is not a `live` query.

The stream is completed with failure if there is a failure in executing the query in the backend journal.

### EventsByPersistenceIdQuery and CurrentEventsByPersistenceIdQuery

`eventsByPersistenceId` and `currentEventsByPersistenceId` is used for retrieving events for
a specific PersistentActor identified by persistenceId.

```scala
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, ActorMaterializer}
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.persistence.query.{ PersistenceQuery, EventEnvelope }
import org.apache.pekko.persistence.postgres.query.scaladsl.PostgresReadJournal

implicit val system: ActorSystem = ActorSystem()
implicit val mat: Materializer = ActorMaterializer()(system)
val readJournal: PostgresReadJournal = PersistenceQuery(system).readJournalFor[PostgresReadJournal](PostgresReadJournal.Identifier)

val willNotCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.eventsByPersistenceId("some-persistence-id", 0L, Long.MaxValue)

val willCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.currentEventsByPersistenceId("some-persistence-id", 0L, Long.MaxValue)
```

You can retrieve a subset of all events by specifying `fromSequenceNr` and `toSequenceNr` or use `0L` and `Long.MaxValue` respectively to retrieve all events. Note that the corresponding sequence number of each event is provided in the `EventEnvelope`, which makes it possible to resume the stream at a later point from a given sequence number.

The returned event stream is ordered by sequence number, i.e. the same order as the PersistentActor persisted the events. The same prefix of stream elements (in same order) are returned for multiple executions of the query, except for when events have been deleted.

The stream is completed with failure if there is a failure in executing the query in the backend journal.

### EventsByTag and CurrentEventsByTag

`eventsByTag` and `currentEventsByTag` are used for retrieving events that were marked with a given
`tag`, e.g. all domain events of an Aggregate Root type.

```scala
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.{Materializer, ActorMaterializer}
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.persistence.query.{ PersistenceQuery, EventEnvelope }
import org.apache.pekko.persistence.postgres.query.scaladsl.PostgresReadJournal

implicit val system: ActorSystem = ActorSystem()
implicit val mat: Materializer = ActorMaterializer()(system)
val readJournal: PostgresReadJournal = PersistenceQuery(system).readJournalFor[PostgresReadJournal](PostgresReadJournal.Identifier)

val willNotCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.eventsByTag("apple", 0L)

val willCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.currentEventsByTag("apple", 0L)
```
