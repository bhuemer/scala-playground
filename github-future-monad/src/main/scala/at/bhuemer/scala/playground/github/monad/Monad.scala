package at.bhuemer.scala.playground.github.monad

/**
 * The millionth definition of a monad in Scala (no need to use scalaz in this example - less than 50 lines in total).
 */
trait Monad[M[_]] {
  def unit[A](a: => A): M[A]
  def map[A, B](ma: M[A])(f: A => B): M[B]
  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
}