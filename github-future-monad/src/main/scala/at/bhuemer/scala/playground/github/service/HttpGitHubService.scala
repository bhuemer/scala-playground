package at.bhuemer.scala.playground.github.service

import at.bhuemer.scala.playground.github.http.HttpRequestor
import at.bhuemer.scala.playground.github.monad.Monad

class HttpGitHubService[Context[_]: Monad](httpRequestor: HttpRequestor[Context]) extends GitHubService[Context] {
  import at.bhuemer.scala.playground.github.monad.syntax._

  override def repositoryNamesFor(owner: String): Context[Option[List[String]]] =
    httpRequestor.request(
      s"https://api.github.com/users/$owner/repos") map
        processResponse { repository: Map[String, String] =>
          // Just extract the name of each repository
          repository.getOrElse("name", "")
        }

  override def commitsFor(owner: String, repository: String): Context[Option[List[Commit]]] =
    httpRequestor.request(
      s"https://api.github.com/repos/$owner/$repository/commits") map
        processResponse { commitWithExtras: Map[String, Map[String, Any]] =>
          val commit = commitWithExtras("commit")
          val committer =
            commit("committer").asInstanceOf[Map[String, String]]
          Commit(
            committer("name"),
            committer("email"),
            committer("date"),
            commit.getOrElse("message", "").asInstanceOf[String]
          )
        }

  /**
   * Takes care of some of the boiler-plate code involved in parsing these raw responses with the builtin JSON parser.
   * Will be partially applied so that we can pass it to map().
   */
  private def processResponse[A, B](body: A => B)(maybeRawResponse: Option[String]): Option[List[B]] = {
    import scala.util.parsing.json.JSON
    for {
      rawResponse <- maybeRawResponse
      jsonResponse <- JSON.parseFull(rawResponse)
      typedResponse <- Some(
        jsonResponse.asInstanceOf[List[A]]
      )
    } yield typedResponse map body
  }

}