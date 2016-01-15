package io.koff.services

import scala.concurrent.Future

class SimpleServiceImpl extends SimpleService{
  override def hello(name: String): Future[String] = Future.successful("hello " + name)
  override def goodBye(name: String): Future[String] = throw new UnsupportedOperationException
}
