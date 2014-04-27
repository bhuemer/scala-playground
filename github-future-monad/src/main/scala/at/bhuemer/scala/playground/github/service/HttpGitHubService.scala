package at.bhuemer.scala.playground.github.service

import at.bhuemer.scala.playground.github.http.HttpRequestor
import at.bhuemer.scala.playground.github.monad.Monad

/**
 * Uses the JSON REST API provided by GitHub to implement our service.
 *
 */
class HttpGitHubService[Context[_]: Monad](httpRequestor: HttpRequestor[Context]) extends GitHubService[Context] {
  // So that we can use map on whatever context we're given
  import at.bhuemer.scala.playground.github.monad.syntax._

  override def repositoryNamesFor(owner: String): Context[Option[List[String]]] =
    request(s"/users/$owner/repos") map
      processResponse { repository: Map[String, String] =>
        // Just extract the name of each repository
        repository.getOrElse("name", "")
      }

  override def followerNamesFor(user: String): Context[Option[List[String]]] =
    request(s"/users/$user/followers") map
      processResponse { follower: Map[String, String] =>
        // Just extract the name (login) of each follower
        follower.getOrElse("login", "")
      }

  override def commitsFor(owner: String, repository: String): Context[Option[List[Commit]]] =
    request(s"/repos/$owner/$repository/commits") map
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

  /** Just extracts the URL for the GitHub API */
  @inline
  private def request(method: String): Context[Option[String]] =
    httpRequestor.request("https://api.github.com" + method)

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