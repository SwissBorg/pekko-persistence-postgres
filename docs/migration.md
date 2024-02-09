---
layout: page
title: Migrations
permalink: /migration
nav_order: 60
---

# Migrations

## Migration from pekko-persistence-jdbc 1.0.0

> :warning: The `akka-persistence-postgres-migration` package was only compatible with `akka-persistence-jdbc` version
> 4.0.0. The `pekko-persistence-jdbc` is based on `akka-persistence-jdbc` version 5.0.0, and as a result, it has been
> temporarily removed from the current project.

## Migration from akka-persistence-postgres to pekko-persistence-postgres

**This guide will walk you through the process of migrating from akka-persistence-postgres to
pekko-persistence-postgres.**
The migration process is straightforward and involves changing package names and configuration settings. Before making
the changes make sure you already migrated from Akka to Pekko following this migration
guide [here](https://pekko.apache.org/docs/pekko/current/project/migration-guides.html).

### Update Package Names

In your code, replace all instances of the `akka` package with `org.apache.pekko`. This includes all import statements
and
fully qualified class names.

### Update Configuration Settings

Next, you need to update your configuration settings. In your `application.conf` file, replace the `akka` block with
a `pekko`
block. Here's an example of how to do this:

Before:

```hocon
akka-persistence-postgres {...}

akka {
  persistence {
    journal {...}
    snapshot-store {...}
  }
}
```

After:

```hocon
pekko-persistence-postgres {...}

pekko {
  persistence {
    journal {...}
    snapshot-store {...}
  }
}
```

### Update Class References

Finally, you need to update class references in your configuration settings. For example, if you have a setting like
this:

```hocon
postgres-journal.dao = "akka.persistence.postgres.journal.dao.NestedPartitionsJournalDao
```

You should change it to:

```hocon
postgres-journal.dao = "org.apache.pekko.persistence.postgres.journal.dao.NestedPartitionsJournalDao
```

## Migration from older akka-persistence-postgres version

If you are using an older version of akka-persistence-postgres, it is recommended to upgrade to the latest version. The
upgrade process involves several steps, which are detailed in
the [migration guide](https://swissborg.github.io/akka-persistence-postgres/migration#migration-from-akka-persistence-postgres-040-to-050).
Please follow the instructions carefully to ensure a smooth transition.