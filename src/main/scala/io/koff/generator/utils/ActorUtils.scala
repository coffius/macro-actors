package io.koff.generator.utils

import scala.concurrent.Future
import scala.util.control.NonFatal
import akka.pattern.pipe

object ActorUtils {
  /**
   * try to evaluate async operation using the by-name parameter.
   * This method will ensure any non-fatal exception is caught and a `Future.failed(...)` is returned.
   */
  def tryIt[T](value: => Future[T]): Future[T] = {
    try{
      value
    } catch {
      case NonFatal(exc) => Future.failed[T](exc)
    }
  }
}
