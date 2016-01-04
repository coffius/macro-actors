package io.koff.actors

import akka.actor.Actor

case class helloMsg(name: String)
case class goodByeMsg(name: String)

class SimpleServiceActor extends Actor {
  def receive = {
    case helloMsg(name) =>
      sender ! "hello " + name
    case goodByeMsg(name) =>
      sender ! "goodbye " + name
  }
}
