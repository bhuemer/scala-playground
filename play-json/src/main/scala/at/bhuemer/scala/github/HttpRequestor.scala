package at.bhuemer.scala.github

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

trait Synchronous[A] { self =>
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

/**
 * Use this if you want to trigger a HTTP Get request and read it all into one big string. Of course, I wouldn't
 * recommend this in practice, but for now it will do in our example.
 */
trait HttpRequestor[Context[_]] {
  def request(url: String): Context[Option[String]]
}

/**
 * Very basic implementation that just uses blocking IO in a given context, i.e. you can still request HTTP data
 * asynchronously, but you will waste threads in doing so (they're going to block). However, this is just an
 * example, so it will do!
 */
class BlockingHttpRequestor[Context[_] : Monad] extends HttpRequestor[Context] {

  def request(url: String): Context[Option[String]] =
    runInContext {
      try withSource(Source.fromURL(url)) {
        source => Some(source.mkString)
      } catch {
        // I know, certainly not the best way of handling exceptions ..
        case _: Exception => None
      }
    }

  /** Just makes sure we're closing sources properly at the end */
  private def withSource[A](source: Source)(f: Source => A): A =
    try f(source) finally source.close()

  /** Makes the method "request" less scary by putting all the monadic stuff in here .. */
  private def runInContext[A](f: => A) = {
    val contextMonadInstance = implicitly[Monad[Context]]
    contextMonadInstance.unit(f)
  }

}

trait Monad[M[_]] {
  def unit[A](a: => A): M[A]
  def map[A, B](ma: M[A])(f: A => B): M[B]
  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
}

trait MonadOps[M[_], A] {
  def map[B](f: A => B): M[B]
  def flatMap[B](f: A => M[B]): M[B]
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

  implicit def monadOps[M[_]: Monad, A](ma: M[A]) = new MonadOps[M, A] {
    override def map[B](f: A => B): M[B] = implicitly[Monad[M]].map(ma)(f)
    override def flatMap[B](f: A => M[B]): M[B] = implicitly[Monad[M]].flatMap(ma)(f)
  }
}

trait GitHubService[Context[_]] {
  def repositorieNamesFor(user: String): Context[Option[List[String]]]
}

class JsonApiGitHubService[Context[_]: Monad](httpRequestor: HttpRequestor[Context]) extends GitHubService[Context] {
  import Monad._
  import scala.util.parsing.json.JSON

  def repositorieNamesFor(user: String): Context[Option[List[String]]] =
    httpRequestor.request(
      s"https://api.github.com/users/${user}/repos") map {
        maybeRawResponse =>
          for {
            rawResponse <- maybeRawResponse
            jsonResponse <- JSON.parseFull(rawResponse)
            repositories <- Some(
              jsonResponse.asInstanceOf[List[Map[String, String]]]
            )
          } yield repositories map (_.getOrElse("name", ""))
      }

}

object GitHubApp {
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    val githubService = new JsonApiGitHubService(new BlockingHttpRequestor[Future])

    Seq("bhuemer", "foobar", "doesthisnameexist", "horst", "spongebob") foreach { name =>
      githubService.repositorieNamesFor(name) map {
        case Some(List())       => println(s"${Thread.currentThread().getName} - Found no repositories for $name.")
        case Some(repositories) => println(s"${Thread.currentThread().getName} - Found repositories: $repositories for $name")
        case None => println(s"${Thread.currentThread().getName} - Could not request repositories for $name.")
      }
    }

    Thread.sleep(10000)
  }
}