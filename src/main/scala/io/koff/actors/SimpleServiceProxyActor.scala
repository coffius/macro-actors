package io.koff.actors

import akka.actor.Actor
import akka.pattern.pipe

import scala.util.control.NonFatal

class SimpleServiceProxyActor(private val internalImpl: AsyncService) extends Actor {
  import context.dispatcher

  def receive = {
//    case hello(name) =>
//      println("proxy actor")
//      val asyncResult = internalImpl.hello(name)
//      val result = Await.result(asyncResult, 10 seconds)
//      sender ! result
    case helloMsg(name) =>
      internalImpl
      .hello(name)
      .map(Right(_))
      .recover {
        case NonFatal(ex) => Left(ex)
      }.pipeTo(sender())
    case goodByeMsg(name) =>
      internalImpl
        .goodBye(name)
        .onSuccess {
          case value => sender ! value
        }
  }
}
