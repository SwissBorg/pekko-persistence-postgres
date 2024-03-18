/*
 * Copyright (C) 2014 - 2019 Dennis Vriend <https://github.com/dnvriend>
 * Copyright (C) 2019 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.persistence.postgres.util

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{FiniteDuration, *}

object BlockingOps {
  implicit class BlockingFutureImplicits[T](val that: Future[T]) extends AnyVal {
    def futureValue(implicit awaitDuration: FiniteDuration = 24.hour): T =
      Await.result(that, awaitDuration)
    def printFutureValue(implicit awaitDuration: FiniteDuration = 24.hour): Unit =
      println(that.futureValue)
  }
}
