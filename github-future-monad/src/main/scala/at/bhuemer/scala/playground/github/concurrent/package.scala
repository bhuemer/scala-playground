package at.bhuemer.scala.playground.github

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}
import at.bhuemer.scala.playground.github.monad.Monad

/**
 *
 */
package object concurrent {

  type Asynchronous[+A] = Future[Try[A]]
  type  Synchronous[+A] =        Try[A]

  implicit def asynchronousInstance(implicit execctx: ExecutionContext) = new Monad[Asynchronous] {
    override def unit[A](a: => A): Asynchronous[A] = Future { Try { a } }
    override def map[A, B](ma: Asynchronous[A])(f: A => B): Asynchronous[B] =
      ma map { tryA => tryA map f }
    override def flatMap[A, B](ma: Asynchronous[A])(f: A => Asynchronous[B]): Asynchronous[B] =
      ma flatMap {
        case Success(a) => f(a)
        case failure => Future {
          failure.asInstanceOf[Try[B]]
        }
      }
  }

  implicit val synchronousInstance = new Monad[Synchronous] {
    override def unit[A](a: => A): Synchronous[A] = Try { a }
    override def map[A, B](ma: Synchronous[A])(f: A => B): Synchronous[B] = ma map f
    override def flatMap[A, B](ma: Synchronous[A])(f: A => Synchronous[B]): Synchronous[B] = ma flatMap f
  }

}
