package at.bhuemer.scala.akkaz

import akka.actor.{Props, Actor, ActorSystem}

trait Par[+A] {
}

object Par {

  private val actorSystem = ActorSystem.create()
  private val workActor = actorSystem.actorOf(Props(new WorkActor))

  def par[A](body: => A): Par[A] = new Par[A] {

  }

  class WorkActor extends Actor {
    def receive = {
      case f: (() => Any) => f()
    }
  }

}

class EchoActor extends Actor {
  def receive = {
    case msg: String => println(Thread.currentThread().getName + ": " + msg)
  }
}

/**
 *
 */
object AkkazApp {

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem.create()

    val echoActorRef = actorSystem.actorOf(Props(new EchoActor))
    echoActorRef ! "Hello World"
    echoActorRef ! "Goodbye"
    Thread.sleep(1000)
    actorSystem.shutdown()
  }

}
