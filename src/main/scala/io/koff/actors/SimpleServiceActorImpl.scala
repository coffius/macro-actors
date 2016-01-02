package io.koff.actors

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import io.koff.services.SimpleService

class SimpleServiceActorImpl(private val actorRef: ActorRef) extends SimpleService {

  private implicit val timeout = Timeout(5 seconds)

  override def hello(name: String): scala.concurrent.Future[String] = {
    actorRef.?(hello(name)).mapTo[String]
  }
  override def goodBye(name: String): scala.concurrent.Future[String] = {
    actorRef.?(goodBye(name)).mapTo[String]
  }

}