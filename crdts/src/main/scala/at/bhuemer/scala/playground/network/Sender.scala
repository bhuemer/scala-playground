package at.bhuemer.scala.playground.network

import akka.actor.{Props, Actor}
import at.bhuemer.scala.playground.support.NonDeterminism

/**
 *
 */
class Sender(receiverProps: Props) extends Actor with NonDeterminism {
  private val receiver = context.actorOf(receiverProps)

  override def receive = {
    case Resend(message) =>
      atLeastOnce {
        receiver ! message
      }

    case message =>
      receiver ! message
  }

}
