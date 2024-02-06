package org.apache.pekko.persistence.postgres.migration.journal

import org.apache.pekko.persistence.PersistentRepr
import org.apache.pekko.serialization.Serialization

import scala.util.Try

private[journal] class OldJournalDeserializer(serialization: Serialization) {

  def deserialize(message: Array[Byte]): Try[PersistentRepr] =
    serialization.deserialize(message, classOf[PersistentRepr])

}
