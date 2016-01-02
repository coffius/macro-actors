package io.koff.services

import akka.actor.Actor


//Actor's message classes
case class helloMsg(name: String)
case class goodByeMsg(name: String)
//Generated Actor
class SimpleServiceProxyActor(private val internalImpl: SimpleService) extends Actor {

  import context.dispatcher

  def receive = {
    case helloMsg(name) =>
      val senderRef = sender()
      val asyncResult = internalImpl.hello(name)
      asyncResult.onSuccess { case value => senderRef ! value }
    case goodByeMsg(name) =>
      val senderRef = sender()
      val asyncResult = internalImpl.goodBye(name)
      asyncResult.onSuccess { case value => senderRef ! value }
  }

}



import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

class SimpleServiceActorImpl(private val actorRef: ActorRef) extends SimpleService {

  private implicit val timeout = Timeout(5 seconds)

  override def hello(name: String): scala.concurrent.Future[String] = {
    actorRef.?(helloMsg(name)).mapTo[String]
  }
  override def goodBye(name: String): scala.concurrent.Future[String] = {
    actorRef.?(goodByeMsg(name)).mapTo[String]
  }

}