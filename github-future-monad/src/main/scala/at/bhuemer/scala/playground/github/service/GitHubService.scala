package at.bhuemer.scala.playground.github.service

case class Commit(author: String, email: String, date: String, message: String)

trait GitHubService[Context[_]] {

  /**
   * Finds a list of all the repositories for the given owner.
   */
  def repositoryNamesFor(owner: String): Context[Option[List[String]]]

  /**
   * Finds a list of all the commits for the given repository.
   */
  def commitsFor(owner: String, repository: String): Context[Option[List[Commit]]]

}
