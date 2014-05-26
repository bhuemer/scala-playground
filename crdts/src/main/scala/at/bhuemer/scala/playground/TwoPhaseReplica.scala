package at.bhuemer.scala.playground

import at.bhuemer.scala.playground.crdts.TwoPhaseSet
import akka.actor.{ActorSystem, Actor}

/**
 *
 */
class TwoPhaseReplica extends Actor {
  private var set = new TwoPhaseSet[Any]()

  override def receive = {
    case TwoPhaseReplica.Add(elem)    => set = set + elem
    case TwoPhaseReplica.Remove(elem) => set = set - elem
    case TwoPhaseReplica.PrintAll => println(set)
  }

}

object TwoPhaseReplica {

  case class Add[A](elem: A)
  case class Remove[A](elem: A)
  case object PrintAll

}

object TwoPhaseReplicaApp {

  import ReplicaProps._

  def main(args: Array[String]) {
    val actorSystem = ActorSystem.create()

    val replicasRef = actorSystem.actorOf(
      replicate(network[TwoPhaseReplica], 5)
    )

    val rnd = new scala.util.Random()
    (1 to 3) foreach { _ =>
      (1 to 10) foreach { i => if (rnd.nextBoolean()) replicasRef ! TwoPhaseReplica.Add(i) }
      (1 to 10) foreach { i => if (rnd.nextBoolean()) replicasRef ! TwoPhaseReplica.Remove(i) }
    }

    // wait a little bit so that the nodes have time to reach consistency
    Thread.sleep(1000)

    replicasRef ! TwoPhaseReplica.PrintAll

    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

}