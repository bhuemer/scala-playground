package at.bhuemer.scala.playground

import akka.actor.{ActorSystem, Actor}

class UnsafeReplica extends Actor {

  /** Represents the state of this replica */
  private var set = Set[Any]()

  override def receive = {
    case UnsafeReplica.Add(elem)    => set = set + elem
    case UnsafeReplica.Remove(elem) => set = set - elem
    case UnsafeReplica.PrintAll => println(set)
  }

}

object UnsafeReplica {

  case class Add[A](elem: A)
  case class Remove[A](elem: A)
  case object PrintAll

}

object UnsafeReplicaApp {

  import ReplicaProps._

  def main(args: Array[String]) {
    val actorSystem = ActorSystem.create()

    val replicasRef = actorSystem.actorOf(
      replicate(network[UnsafeReplica])
    )

    (1 to 10) foreach { _ =>
      (1 to 10) foreach { replicasRef ! UnsafeReplica.Add(_) }
      (1 to 10) foreach { replicasRef ! UnsafeReplica.Remove(_) }
    }

    /*
     * Last time around this produced something like:
     *
     * Set(1)
     * Set(6, 2)
     * Set(10, 7)
     * Set(5, 6, 9, 2, 8)
     * Set(5, 8)
     *
     * .. i.e. inconsistent replicas ..
     */
    replicasRef ! UnsafeReplica.PrintAll

    Thread.sleep(100)
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

}