/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.journal

import com.typesafe.config.Config
import org.apache.pekko.Done
import org.apache.pekko.actor.{ActorSystem, ExtendedActorSystem}
import org.apache.pekko.pattern.pipe
import org.apache.pekko.persistence.{AtomicWrite, PersistentRepr}
import org.apache.pekko.persistence.journal.AsyncWriteJournal
import org.apache.pekko.persistence.postgres.config.JournalConfig
import org.apache.pekko.persistence.postgres.db.{SlickDatabase, SlickExtension}
import org.apache.pekko.persistence.postgres.journal.PostgresAsyncWriteJournal.{InPlaceUpdateEvent, WriteFinished}
import org.apache.pekko.persistence.postgres.journal.dao.{JournalDao, JournalDaoWithUpdates}
import org.apache.pekko.serialization.{Serialization, SerializationExtension}
import org.apache.pekko.stream.{Materializer, SystemMaterializer}
import slick.jdbc.JdbcBackend._

import java.util.{HashMap => JHMap, Map => JMap}
import scala.collection.immutable._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object PostgresAsyncWriteJournal {
  private case class WriteFinished(pid: String, f: Future[_])

  /** Extra Plugin API: May be used to issue in-place updates for events. To be used only for data migrations such as
    * "encrypt all events" and similar operations.
    *
    * The write payload may be wrapped in a [[org.apache.pekko.persistence.journal.Tagged]], in which case the new tags
    * will be skipped and the old tags remain unchanged.
    */
  final case class InPlaceUpdateEvent(persistenceId: String, seqNr: Long, write: AnyRef)
}

class PostgresAsyncWriteJournal(config: Config) extends AsyncWriteJournal {

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val mat: Materializer = SystemMaterializer(system).materializer
  val journalConfig = new JournalConfig(config)

  val slickDb: SlickDatabase = SlickExtension(system).database(config)
  def db: Database = slickDb.database

  val journalDao: JournalDao = {
    val fqcn = journalConfig.pluginConfig.dao
    val args = Seq(
      (classOf[Database], db),
      (classOf[JournalConfig], journalConfig),
      (classOf[Serialization], SerializationExtension(system)),
      (classOf[ExecutionContext], ec),
      (classOf[Materializer], mat)
    )
    system.asInstanceOf[ExtendedActorSystem].dynamicAccess.createInstanceFor[JournalDao](fqcn, args) match {
      case Success(dao)   => dao
      case Failure(cause) => throw cause
    }
  }
  // only accessed if we need to perform Updates -- which is very rarely
  def journalDaoWithUpdates: JournalDaoWithUpdates =
    journalDao match {
      case upgraded: JournalDaoWithUpdates => upgraded
      case _ =>
        throw new IllegalStateException(
          s"The ${journalDao.getClass} does NOT implement [JournalDaoWithUpdates], " +
            s"which is required to perform updates of events! Please configure a valid update capable DAO (e.g. the default [FlatJournalDao]."
        )
    }

  // readHighestSequence must be performed after pending write for a persistenceId
  // when the persistent actor is restarted.
  private val writeInProgress: JMap[String, Future[_]] = new JHMap

  override def asyncWriteMessages(messages: Seq[AtomicWrite]): Future[Seq[Try[Unit]]] = {

    // add timestamp to all payloads in all AtomicWrite messages
    val timedMessages =
      messages.map { atomWrt =>
        // since they are all persisted atomically,
        // all PersistentRepr on the same atomic batch gets the same timestamp
        val now = System.currentTimeMillis()
        atomWrt.copy(payload = atomWrt.payload.map(pr => pr.withTimestamp(now)))
      }

    val future = journalDao.asyncWriteMessages(timedMessages)
    val persistenceId = timedMessages.head.persistenceId
    writeInProgress.put(persistenceId, future)
    future.onComplete(_ => self ! WriteFinished(persistenceId, future))
    future
  }

  override def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] =
    journalDao.delete(persistenceId, toSequenceNr)

  override def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    def fetchHighestSeqNr() = journalDao.highestSequenceNr(persistenceId, fromSequenceNr)
    writeInProgress.get(persistenceId) match {
      case null => fetchHighestSeqNr()
      case f    =>
        // we must fetch the highest sequence number after the previous write has completed
        // If the previous write failed then we can ignore this
        f.recover { case _ => () }.flatMap(_ => fetchHighestSeqNr())
    }
  }

  private def asyncUpdateEvent(persistenceId: String, sequenceNr: Long, message: AnyRef): Future[Done] = {
    journalDaoWithUpdates.update(persistenceId, sequenceNr, message)
  }

  override def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long, max: Long)(
      recoveryCallback: PersistentRepr => Unit
  ): Future[Unit] =
    journalDao
      .messagesWithBatch(persistenceId, fromSequenceNr, toSequenceNr, journalConfig.daoConfig.replayBatchSize, None)
      .take(max)
      .mapAsync(1)(reprAndOrdNr => Future.fromTry(reprAndOrdNr))
      .runForeach { case (repr, _) =>
        recoveryCallback(repr)
      }
      .map(_ => ())

  override def postStop(): Unit = {
    if (slickDb.allowShutdown) {
      // Since a (new) db is created when this actor (re)starts, we must close it when the actor stops
      db.close()
    }
    super.postStop()
  }

  override def receivePluginInternal: Receive = {
    case WriteFinished(persistenceId, future) =>
      writeInProgress.remove(persistenceId, future)
    case InPlaceUpdateEvent(pid, seq, write) =>
      asyncUpdateEvent(pid, seq, write).pipeTo(sender())
  }
}
