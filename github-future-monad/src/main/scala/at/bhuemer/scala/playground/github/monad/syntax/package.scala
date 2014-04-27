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

  def sequence[M[_]: Monad, A](as: List[M[A]]): M[List[A]] = {
    as match {
      case head :: tail => monadOps(head).flatMap({ a: A =>
        monadOps(sequence(tail)).map({ as: List[A] =>
          a :: as
        })
      })
      case Nil =>
        pure(Nil.asInstanceOf[List[A]])
    }
  }

  def pure[M[_]: Monad, A](a: A): M[A] = implicitly[Monad[M]].unit(a)

  implicit def monadOps[M[_]: Monad, A](ma: M[A]) = new MonadOps[M, A] {
    override def map[B](f: A => B): M[B] = implicitly[Monad[M]].map(ma)(f)
    override def flatMap[B](f: A => M[B]): M[B] = implicitly[Monad[M]].flatMap(ma)(f)
  }

}
