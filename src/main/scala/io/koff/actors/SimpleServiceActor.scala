package io.koff.actors

import akka.actor.Actor

case class hello(name: String)
case class goodBye(name: String)

class SimpleServiceActor extends Actor {
  def receive = {
    case hello(name) =>
      sender ! "hello " + name
    case goodBye(name) =>
      sender ! "goodbye " + name
  }
}
