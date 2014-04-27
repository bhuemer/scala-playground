package at.bhuemer.scala.playground.github

import scala.concurrent.Future

/**
 *
 */
package object concurrent {

  type Asynchronous[+A] = Future[A]

  /**
   * A dummy context that can be used instead of future. This one doesn't do anything (it's really just an ID monad
   * so to say), but when we use this we can switch from asynchronous to synchronous evaluation. Also, I'm deliberately
   * not just using "type Synchronous[+A] = A" to avoid any kind of confusion as to which method should be called
   * when working with an instance of Synchronous[ Option[..] ] - the compiler would like to use Option's map here.
   */
  trait Synchronous[+A] { self =>
    def get: A

    def map[B](f: A => B): Synchronous[B] = Synchronous { f(self.get) }
    def flatMap[B](f: A => Synchronous[B]) = f(self.get)
  }

  object Synchronous {
    def apply[A](f: => A): Synchronous[A] = new Synchronous[A] {
      private val content = f // to force evaluation
      override def get: A = content
    }
  }

}
