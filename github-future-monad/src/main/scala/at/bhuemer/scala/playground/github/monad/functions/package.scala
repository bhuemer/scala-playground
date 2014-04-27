package at.bhuemer.scala.playground.github.monad

/**
 *
 */
package object functions {

  def sequence[M[_]: Monad, A](as: List[M[A]]): M[List[A]] = {
    import at.bhuemer.scala.playground.github.monad.syntax._
    as match {
      case Nil =>
        pure(Nil.asInstanceOf[List[A]])

      case head :: tail =>
        for {
          a <- head
          as <- sequence(tail)
        } yield a :: as
    }
  }

  def pure[M[_]: Monad, A](a: => A): M[A] =
    implicitly[Monad[M]].unit(a)

}
