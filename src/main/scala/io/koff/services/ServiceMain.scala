package io.koff.services

import scala.concurrent.Await
import scala.concurrent.duration._

object ServiceMain {
  def main(args: Array[String]) {
    val service: SimpleService = new SimpleServiceImpl()
    val asyncResult = service.hello("scala")
    val result = Await.result(asyncResult, 10 seconds)
    println("result: " + result)
  }
}
