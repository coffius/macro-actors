package io.koff.actors

import akka.actor.Actor
import akka.pattern.pipe
import io.koff.services.SimpleService
import io.koff.generator.utils.ActorUtils._

class SimpleServiceProxyActor(private val internalImpl: SimpleService) extends Actor {
  import context.dispatcher

  def receive = {
//    case hello(name) =>
//      println("proxy actor")
//      val asyncResult = internalImpl.hello(name)
//      val result = Await.result(asyncResult, 10 seconds)
//      sender ! result
    case helloMsg(name) => internalImpl.hello(name).pipeTo(sender())
    case goodByeMsg(name) => tryIt(internalImpl.goodBye(name)).pipeTo(sender())
  }

}
