package io.koff.actors

import akka.actor.Actor
import akka.pattern.pipe

class SimpleServiceProxyActor(private val internalImpl: AsyncService) extends Actor {
  import context.dispatcher

  def receive = {
//    case hello(name) =>
//      println("proxy actor")
//      val asyncResult = internalImpl.hello(name)
//      val result = Await.result(asyncResult, 10 seconds)
//      sender ! result
    case hello(name) => internalImpl.hello(name).pipeTo(sender())
    case goodBye(name) =>
      internalImpl
        .goodBye(name)
        .onSuccess {
          case value => sender ! value
        }
  }
}
