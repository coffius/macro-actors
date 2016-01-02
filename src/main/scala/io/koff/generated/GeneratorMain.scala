package io.koff.generated

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import io.koff.services.{SimpleService, SimpleServiceImpl}

import scala.concurrent.Await
import scala.concurrent.duration._
import io.koff.actors.hello

import scala.language.postfixOps

object GeneratorMain {
  implicit val timeout = Timeout(5 seconds)

  def main(args: Array[String]) {
    val system = ActorSystem("akka-system")
    val impl = new SimpleServiceImpl
    val service = ActorGenerator.gen[SimpleService, SimpleServiceImpl](system, impl)
    val result = Await.result(service.hello("scala"), 10 seconds)
    println(s"result: $result")
  }
}
