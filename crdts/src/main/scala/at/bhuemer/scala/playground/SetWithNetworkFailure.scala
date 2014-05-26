package at.bhuemer.scala.playground

import akka.actor.{Props, ActorSystem, Actor}
import at.bhuemer.scala.playground.network.{Sender, Receiver}
import akka.routing.BroadcastPool
import scala.reflect.ClassTag
import at.bhuemer.scala.playground.crdts.TwoPhaseSet

/**
 *
 */
object SetWithNetworkFailure {

  case class Add[A](elem: A)
  case class Remove[A](elem: A)
  case object PrintAll

  class UnsafeReplica extends Actor {
    private var set = Set[Any]()

    override def receive = {
      case Add(elem)    => set = set + elem
      case Remove(elem) => set = set - elem
      case PrintAll => println(set.toString())
    }

  }

  class TwoPhaseReplica extends Actor {
    private var set = new TwoPhaseSet[Any]()

    override def receive = {
      case Add(elem)    => set = set + elem
      case Remove(elem) => set = set - elem
      case PrintAll => println(set)
    }
  }
//
//  def main(args: Array[String]) {
//    val actorSystem = ActorSystem.create()
//
//    val replicasRef = actorSystem.actorOf(replicas[TwoPhaseReplica](actorSystem))
//
//    (1 to 10) foreach { _ =>
//      (1 to 10) foreach { replicasRef ! Add(_) }
//      (1 to 10) foreach { replicasRef ! Remove(_) }
//    }
//    replicasRef ! PrintAll
//
//    Thread.sleep(1000)
//
//    actorSystem.shutdown()
//    actorSystem.awaitTermination()
//  }
//
//  def replicas[T <: Actor: ClassTag](actorSystem: ActorSystem) = BroadcastPool(5).props(Props({
//    val replicaRef = actorSystem.actorOf(Props[T])
//    val receiverRef = actorSystem.actorOf(Props(new Receiver(replicaRef)))
//    new Sender(receiverRef)
//  }))

}
