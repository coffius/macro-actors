package io.koff.actors

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import io.koff.services.SimpleServiceImpl

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object ActorMain {
  implicit val timeout = Timeout(5 seconds)
  def main(args: Array[String]) {
    val system = ActorSystem("akka-system")
    val impl = new AsyncServiceImpl
    val actor = system.actorOf(Props(classOf[ProblemActor], impl), "SimpleServiceProxyActor")
    //it's ok
    val result = ask(actor, hello("scala")).mapTo[String]
    //there is AskTimeoutException
    val badResult = ask(actor, goodBye("scala")).mapTo[String]
    val resultString = Await.result(result, 10 seconds)
    val bad = Await.result(badResult, 10 seconds)
    println("result: " + resultString)
    Await.result(system.terminate(), 10 seconds)
  }
}
