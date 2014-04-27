package at.bhuemer.scala.playground.github.monad

import scala.concurrent.{Future, ExecutionContext}
import at.bhuemer.scala.playground.github.concurrent.Synchronous

/**
 * The millionth definition of a monad in Scala (no need to use scalaz in this example - less than 50 lines in total).
 */
trait Monad[M[_]] {
  def unit[A](a: => A): M[A]
  def map[A, B](ma: M[A])(f: A => B): M[B]
  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
}

object Monad {

  implicit def futureInstance(implicit execctx: ExecutionContext) = new Monad[Future] {
    override def unit[A](a: => A): Future[A] = Future { a }
    override def map[A, B](ma: Future[A])(f: (A) => B): Future[B] = ma map f
    override def flatMap[A, B](ma: Future[A])(f: (A) => Future[B]): Future[B] = ma flatMap f
  }

  implicit val synchronousInstance = new Monad[Synchronous] {
    override def unit[A](a: => A): Synchronous[A] = Synchronous { a }
    override def map[A, B](ma: Synchronous[A])(f: A => B): Synchronous[B] = ma map f
    override def flatMap[A, B](ma: Synchronous[A])(f: A => Synchronous[B]): Synchronous[B] = ma flatMap f
  }

}