package io.koff.actors

import akka.actor.{Props, ActorSystem}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object ProxyMain {
  def main(args: Array[String]) {
    val system = ActorSystem("akka-system")
    val impl = new AsyncServiceImpl
    val proxyActorRef = system.actorOf(Props(classOf[SimpleServiceProxyActor], impl))
    val actorImpl: AsyncService = new SimpleServiceActorImpl(proxyActorRef)

    val asyncResult = actorImpl.hello("scala")
    val result = Await.result(asyncResult, 10 seconds)
    println(s"result: $result")
  }
}
