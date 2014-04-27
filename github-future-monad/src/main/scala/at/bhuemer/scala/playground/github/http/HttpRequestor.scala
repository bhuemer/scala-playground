package at.bhuemer.scala.playground.github.http

import scala.util.Try

/**
 * Use this if you want to trigger a HTTP Get request and read it all into one big string. Of course, I wouldn't
 * recommend this in practice, but for now it will do in our example.
 */
trait HttpRequestor[Context[_]] {
  def request(url: String): Context[Try[String]]
}