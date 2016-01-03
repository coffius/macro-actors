package io.koff.actors

import akka.actor.Actor
import io.koff.services.SimpleService

import scala.concurrent.Await
import scala.concurrent.duration._

class SimpleServiceProxyActor(private val internalImpl: AsyncService) extends Actor {
  import context.dispatcher

  def receive = {
//    case hello(name) =>
//      println("proxy actor")
//      val asyncResult = internalImpl.hello(name)
//      val result = Await.result(asyncResult, 10 seconds)
//      sender ! result
    case hello(name) =>
      val senderRef = sender()
      val asyncResult = internalImpl.hello(name)
      asyncResult.onSuccess { case value => senderRef ! value }
    case goodBye(name) =>
      internalImpl
        .goodBye(name)
        .onSuccess {
          case value => sender ! value
        }
  }
}
