package at.bhuemer.scala.playground

import akka.actor.Props
import at.bhuemer.scala.playground.network.{Receiver, Sender}
import akka.routing.BroadcastPool

/**
 *
 */
object ReplicaProps {

  def replicate(props: Props, numberOfReplicas: Int = 5): Props =
    BroadcastPool(numberOfReplicas).props(props)

  /** Causes messages to arrive out-of-order, potentially many times .. i.e. all kinds of nasty network things happen. */
  def network(props: Props): Props = {
    val receiverProps = Props(new Receiver(props))
    val senderProps = Props(new Sender(receiverProps))
    senderProps
  }

}
