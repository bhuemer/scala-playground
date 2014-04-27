package at.bhuemer.scala.playground.github.service

import scala.util.Try

case class Commit(author: String, email: String, date: String, message: String)

trait GitHubService[Context[_]] {

  /**
   * Finds a list of all the repositories for the given owner.
   */
  def repositoryNamesFor(owner: String): Context[Try[List[String]]]

  /**
   * Finds all followers for the given users.
   */
  def followerNamesFor(user: String): Context[Try[List[String]]]

  /**
   * Finds a list of all the commits for the given repository.
   */
  def commitsFor(owner: String, repository: String): Context[Try[List[Commit]]]

}
