package at.bhuemer.scala.playground.network

import akka.actor.Actor


class Receiver extends Actor {
  override def receive = {
    case "foo" =>
  }
}
