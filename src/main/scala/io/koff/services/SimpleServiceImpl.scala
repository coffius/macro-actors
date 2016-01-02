package io.koff.services

import scala.concurrent.Future

class SimpleServiceImpl extends SimpleService{
  override def hello(name: String): Future[String] = {
    Future.successful("hello " + name)
  }

  private def privateMethod(param: String): String = {
    throw new UnsupportedOperationException
  }

  override def goodBye(name: String): Future[String] = {
    Future.successful("goodbye " + name)
  }
}
