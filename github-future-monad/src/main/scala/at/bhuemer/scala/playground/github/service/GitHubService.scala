package at.bhuemer.scala.playground.github.service

trait GitHubService[Context[_]] {
  def repositorieNamesFor(user: String): Context[Option[List[String]]]
}
