package at.bhuemer.scala.playground.github.service

import at.bhuemer.scala.playground.github.http.HttpRequestor
import at.bhuemer.scala.playground.github.monad.Monad
import scala.util.{Failure, Success, Try}

/**
 * Uses the JSON REST API provided by GitHub to implement our service.
 *
 */
class HttpGitHubService[Context[_]: Monad](httpRequestor: HttpRequestor[Context]) extends GitHubService[Context] {
  // So that we can use map on whatever context we're given
  import at.bhuemer.scala.playground.github.monad.syntax._

  override def repositoryNamesFor(owner: String): Context[Try[List[String]]] =
    request(s"/users/$owner/repos") {
      repository: Map[String, String] =>
        // Just extract the name of each repository
        repository.getOrElse("name", "")
      }

  override def followerNamesFor(user: String): Context[Try[List[String]]] =
    request(s"/users/$user/followers") {
      follower: Map[String, String] =>
        // Just extract the name (login) of each follower
        follower.getOrElse("login", "")
      }

  override def commitsFor(owner: String, repository: String): Context[Try[List[Commit]]] =
    request(s"/repos/$owner/$repository/commits") {
      commitWithExtras: Map[String, Map[String, Any]] =>
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
   * It also assumes that the request will return a list of something (list of repositories, list of followers, etc.),
   * but then .. I haven't found anything in the GitHub API that doesn't return a list.
   */
  private def request[A, B](method: String)(body: A => B): Context[Try[List[B]]] = {
    import scala.util.parsing.json.JSON
    httpRequestor.request("https://api.github.com" + method) map {
      tryRawResponse =>
        for {
          rawResponse <- tryRawResponse
          jsonResponse <- JSON.parseFull(rawResponse) match {
            case Some(json) => Success(json)
            case None => Failure(
              new IllegalArgumentException(s"Invalid JSON: $rawResponse")
            )
          }
          parsedResponse <- Try(
            jsonResponse.asInstanceOf[List[A]] map body
          )
        } yield parsedResponse
      }
  }

}