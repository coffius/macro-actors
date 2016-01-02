package io.koff.services

import scala.concurrent.Future

trait SimpleService {
  def hello(name: String): Future[String]
  def goodBye(name: String): Future[String]
}
