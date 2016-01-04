package io.koff.actors

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import io.koff.services.SimpleService

class SimpleServiceActorImpl(private val actorRef: ActorRef) extends AsyncService {
  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit val timeout = Timeout(5 seconds)

  override def hello(name: String): scala.concurrent.Future[String] = {
    actorRef.?(helloMsg(name)).mapTo[Either[Throwable, String]].flatMap {
      case Right(value) => Future.successful(value)
      case Left(exc) => Future.failed(exc)
    }
  }
  override def goodBye(name: String): scala.concurrent.Future[String] = {
    actorRef.?(goodByeMsg(name)).mapTo[Either[Throwable, String]].flatMap {
      case Right(value) => Future.successful(value)
      case Left(exc) => Future.failed(exc)
    }
  }

}