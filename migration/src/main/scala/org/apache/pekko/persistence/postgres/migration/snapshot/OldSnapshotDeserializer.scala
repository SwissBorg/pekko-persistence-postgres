package org.apache.pekko.persistence.postgres.migration.snapshot

import org.apache.pekko.persistence.serialization.Snapshot
import org.apache.pekko.serialization.Serialization

import scala.util.Try

private[snapshot] class OldSnapshotDeserializer(serialization: Serialization) {

  def deserialize(rawSnapshot: Array[Byte]): Try[Any] = {
    serialization.deserialize(rawSnapshot, classOf[Snapshot]).map(_.data)
  }
}
