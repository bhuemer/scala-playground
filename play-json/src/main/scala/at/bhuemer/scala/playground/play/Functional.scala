package at.bhuemer.scala.playground.play

import scala.collection.TraversableOnce

//trait Applicative[F[_]] {
//  def pure[A](a: A): F[A]
//  def apply[A, B](f: F[A => B]): F[A] => F[B]
//}
//
//object Applicative {
//
//  implicit val optionInstance = new Applicative[Option] {
//    override def pure[A](a: A): Option[A] = Some(a)
//    override def apply[A, B](of: Option[A => B]): Option[A] => Option[B] = {
//      oa =>
//        for {
//          a <- oa
//          f <- of
//        } yield f(a)
//    }
//  }
//
//  def main(args: Array[String]): Unit = {
//
//  }
//
//}

/**
 * Similar to a functor trait, only that this
 * takes a function B => A rather than A => B.
 */
trait Contravariant[F[_]] {
  def comap[A,B](f: B => A)(fa: F[A]): F[B]
}

/**
 * Trait that defines the comap method in infix notation.
 */
trait ContravariantOps[F[_], A] {
  def comap[B](f: B => A): F[B]
}

/**
 * Defines implicit conversions so that we can use comap in the infix notation.
 */
object Contravariant {
  implicit def contravariantOps[F[_] : Contravariant, A](cf: F[A]) =
    new ContravariantOps[F, A] {
      override def comap[B](f: B => A): F[B] =
        implicitly[Contravariant[F]].comap(f)(cf)
    }
}

/**
 * Predicate is contravariant in A as subtyping is reversed.
 */
trait Predicate[-A] {
  def apply(a: A): Boolean
}

object Predicate {

  /**
   * We could have simply defined 'type Predicate[-A] = A => Boolean'. This will
   * make it as convenient to construct predicates - they're just unary functions
   * returning Boolean values.
   */
  def asPredicate[A](f: A => Boolean): Predicate[A] = new Predicate[A] {
    override def apply(a: A): Boolean = f(a)
  }

  /**
   * Given a Predicate[A] and a function B => A we can produce a Predicate[B],
   * so Predicate is a Cofunctor (leaving aside the cofunctor laws for now ..).
   */
  implicit val predicateCofunctor = new Contravariant[Predicate] {
    override def comap[A, B](f: B => A)(fa: Predicate[A]): Predicate[B] =
      asPredicate[B](f andThen fa.apply)
  }

}

object CofunctorApp {
  def main(args: Array[String]) {
    import Contravariant._
    import Predicate._

    val nonZeroPredicate = asPredicate[Number] { _ != 0 }
    val nonEmpyPredicate = nonZeroPredicate comap { l: List[_] => l.size }

    val falsePredicate = { _: Any => false }

    println(nonEmpyPredicate(List()))
    println(nonEmpyPredicate(List(1, 2)))
  }
}