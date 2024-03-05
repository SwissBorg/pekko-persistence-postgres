---
title: About the plugin
nav_order: 0
---

The Pekko Persistence Postgres plugin allows for using Postgres database as backend for [Pekko Persistence](https://pekko.apache.org/docs/pekko/current/typed/persistence.html) and [Pekko Persistence Query](https://pekko.apache.org/docs/pekko/current/persistence-query.html).

pekko-persistence-postgres writes journal and snapshot entries to a configured PostgreSQL store. It implements the full pekko-persistence-query API and is therefore very useful for implementing DDD-style application models using Pekko and Scala for creating reactive applications.

It’s been originally created as a fork of [Akka Persistence JDBC plugin](https://github.com/akka/akka-persistence-jdbc) 4.0.0, focused on PostgreSQL features such as partitions, arrays, BRIN indexes and others. Many parts of this doc have been adopted from the original [project page](https://doc.akka.io/docs/akka-persistence-jdbc/4.0.0/index.html).

The main goal is to keep index size and memory consumption on a moderate level while being able to cope with an increasing data volume.

## Installation

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

> :warning: Since Pekko [does not allow mixed versions](https://nightlies.apache.org/pekko/docs/pekko/1.0.2/docs/common/binary-compatibility-rules.html#mixed-versioning-is-not-allowed) in a project, Pekko dependencies are marked a `Provided`. This means that your application `libraryDependencies` needs to directly include Pekko as a dependency. The minimal supported Pekko version is 1.0.0.  


## Source code

Source code for this plugin can be found on [GitHub](https://github.com/SwissBorg/pekko-persistence-postgres).

## Contribution policy

Contributions via GitHub pull requests are gladly accepted. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## Contributors
List of all contributors can be found on [GitHub](https://github.com/SwissBorg/pekko-persistence-postgres/graphs/contributors).

## Sponsors

Development and maintenance of pekko-persistence-postgres is sponsored by:

![SoftwareMill]({{ 'assets/softwaremill-logo.png' | absolute_url }})

[SoftwareMill](https://softwaremill.com) is a software development and consulting company. We help clients scale their business through software. Our areas of expertise include backends, distributed systems, blockchain, machine learning and data analytics.

![SwissBorg]({{ 'assets/swissborg-logo.png' | absolute_url }})

[SwissBorg](https://swissborg.com) makes managing your crypto investment easy and helps control your wealth.

## License

This source code is made available under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).
