/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.query

import com.typesafe.config.Config
import org.apache.pekko.actor.ExtendedActorSystem
import org.apache.pekko.persistence.query.ReadJournalProvider

class PostgresReadJournalProvider(system: ExtendedActorSystem, config: Config, configPath: String)
    extends ReadJournalProvider {
  override def scaladslReadJournal(): scaladsl.PostgresReadJournal =
    new scaladsl.PostgresReadJournal(config, configPath)(system)

  override def javadslReadJournal(): javadsl.PostgresReadJournal =
    new javadsl.PostgresReadJournal(scaladslReadJournal())
}
