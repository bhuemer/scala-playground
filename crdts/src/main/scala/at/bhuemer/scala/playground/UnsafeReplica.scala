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

    val rnd = new scala.util.Random()
    (1 to 3) foreach { _ =>
      (1 to 10) foreach { i => if (rnd.nextBoolean()) replicasRef ! UnsafeReplica.Add(i) }
      (1 to 10) foreach { i => if (rnd.nextBoolean()) replicasRef ! UnsafeReplica.Remove(i) }
    }

    // wait a little bit so that the nodes have time to reach consistency
    Thread.sleep(1000)

    /*
     * Last time around this produced something like:
     *
     * Set(10, 7, 3, 8, 4)
     * Set(6, 2, 8)
     * Set(5, 1, 6, 9, 8, 4)
     * Set(6, 9, 7, 8, 4)
     * Set(5, 10, 1, 2, 3, 8)
     *
     * .. i.e. inconsistent replicas ..
     */
    replicasRef ! UnsafeReplica.PrintAll

    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

}