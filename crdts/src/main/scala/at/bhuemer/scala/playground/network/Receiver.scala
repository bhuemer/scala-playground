package at.bhuemer.scala.playground.network

import akka.actor.{Props, Actor}
import at.bhuemer.scala.playground.support.NonDeterminism

case class Resend[A](message: A)

class Receiver(receiverProps: Props) extends Actor with NonDeterminism {
  private val receiver = context.actorOf(receiverProps)

  override def receive = {
    case message =>
      either {
        Thread.sleep(10) // Wait a little bit to let other messages race ahead
        sender() ! Resend(message)
      } or {
        receiver ! message
      }
  }

}

