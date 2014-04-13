package at.bhuemer.scala.playground.play

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