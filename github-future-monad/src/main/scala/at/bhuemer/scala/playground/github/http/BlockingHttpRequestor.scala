package at.bhuemer.scala.playground.github.http

import scala.io.Source
import at.bhuemer.scala.playground.github.monad.Monad
import at.bhuemer.scala.playground.github.monad.functions.{pure => runInContext}
import scala.util.Try

/**
 * Very basic implementation that just uses blocking IO in a given context, i.e. you can still request HTTP data
 * asynchronously, but you will waste threads in doing so (they're going to block). However, this is just an
 * example, so it will do!
 */
class BlockingHttpRequestor[Context[_] : Monad] extends HttpRequestor[Context] {

  def request(url: String): Context[Try[String]] =
    runInContext {
      Try(
        withSource(Source.fromURL(url)) {
          source => source.mkString
        }
      )
    }

  /** Just makes sure we're closing sources properly at the end */
  private def withSource[A](source: Source)(f: Source => A): A =
    try f(source) finally source.close()

}