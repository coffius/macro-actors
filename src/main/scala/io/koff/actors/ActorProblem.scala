package io.koff.actors

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

trait AsyncService {
  def hello(name: String): Future[String]
  def goodBye(name: String): Future[String]
}

class AsyncServiceImpl extends AsyncService{
  override def hello(name: String): Future[String] = Future.successful("hello " + name)
  override def goodBye(name: String): Future[String] = Future.successful("goodbye " + name)
}

class ProblemActor(private val internalImpl: AsyncService) extends Actor {
  import context.dispatcher

  def receive = {
    case helloMsg(name) =>
      val senderRef = sender()
      val asyncResult = internalImpl.hello(name)
      asyncResult.onSuccess { case value => senderRef ! value }
    case goodByeMsg(name) =>
      internalImpl.goodBye(name).pipeTo(sender())
  }
}

object ActorProblem {
  implicit val timeout = Timeout(5 seconds)
  def main(args: Array[String]) {
    val system = ActorSystem("akka-system")
    val impl = new AsyncServiceImpl
    val actor = system.actorOf(Props(classOf[ProblemActor], impl), "SimpleServiceProxyActor")
    //it's ok
    val result = ask(actor, helloMsg("scala")).mapTo[String]
    val resultString = Await.result(result, 10 seconds)

    //there is AskTimeoutException
    val badResult = ask(actor, goodByeMsg("scala")).mapTo[String]
    val bad = Await.result(badResult, 10 seconds)

    println("result: " + bad)
    Await.result(system.terminate(), 10 seconds)
  }
}
