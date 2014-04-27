package at.bhuemer.scala.playground.github.monad

/**
 * So that we can import special monad syntax in a way that makes it clear.
 */
package object syntax {

  /**
   * Trait that defines the operations of a monad again in infix notation.
   */
  trait MonadOps[M[_], A] {
    def map[B](f: A => B): M[B]
    def flatMap[B](f: A => M[B]): M[B]
  }

  implicit def monadOps[M[_]: Monad, A](ma: M[A]) = new MonadOps[M, A] {
    override def map[B](f: A => B): M[B] = implicitly[Monad[M]].map(ma)(f)
    override def flatMap[B](f: A => M[B]): M[B] = implicitly[Monad[M]].flatMap(ma)(f)
  }

}
