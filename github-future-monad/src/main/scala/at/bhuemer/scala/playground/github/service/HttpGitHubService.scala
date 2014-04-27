package at.bhuemer.scala.playground.github.service

import at.bhuemer.scala.playground.github.http.HttpRequestor
import at.bhuemer.scala.playground.github.monad.Monad

class HttpGitHubService[Context[_]: Monad](httpRequestor: HttpRequestor[Context]) extends GitHubService[Context] {
  import at.bhuemer.scala.playground.github.monad.syntax._
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
        // Extract the name of each repository map
        } yield repositories map (_.getOrElse("name", ""))
    }

}