/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.configuration

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.persistence.postgres.SimpleSpec
import org.apache.pekko.persistence.postgres.db.SlickExtension

class JNDIConfigTest extends SimpleSpec {
  "JNDI config" should "read the config and throw NoInitialContextException in case the JNDI resource is not available" in {
    withActorSystem("jndi-application.conf") { system =>
      val jdbcJournalConfig = system.settings.config.getConfig("postgres-journal")
      val slickExtension = SlickExtension(system)
      intercept[javax.naming.NoInitialContextException] {
        // Since the JNDI resource is not actually available we expect a NoInitialContextException
        // This is an indication that the application actually attempts to load the configured JNDI resource
        slickExtension.database(jdbcJournalConfig).database
      }
    }
  }

  "JNDI config for shared databases" should "read the config and throw NoInitialContextException in case the JNDI resource is not available" in {
    withActorSystem("jndi-shared-db-application.conf") { system =>
      val jdbcJournalConfig = system.settings.config.getConfig("postgres-journal")
      val slickExtension = SlickExtension(system)
      intercept[javax.naming.NoInitialContextException] {
        // Since the JNDI resource is not actually available we expect a NoInitialContextException
        // This is an indication that the application actually attempts to load the configured JNDI resource
        slickExtension.database(jdbcJournalConfig).database
      }
    }
  }

  def withActorSystem(config: String)(f: ActorSystem => Unit): Unit = {
    val cfg = ConfigFactory.load(config)
    val system = ActorSystem("test", cfg)

    try {
      f(system)
    } finally {
      system.terminate().futureValue
    }
  }
}
