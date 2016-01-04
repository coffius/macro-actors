package io.koff.actors

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.koff.services.SimpleService

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