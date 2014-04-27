package at.bhuemer.scala.playground.github

import at.bhuemer.scala.playground.github.service.HttpGitHubService
import at.bhuemer.scala.playground.github.http.BlockingHttpRequestor
import at.bhuemer.scala.playground.github.concurrent.{Synchronous, Asynchronous}

/**
 *
 */
object GitHubDemoApp {

  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
    val githubService = new HttpGitHubService(new BlockingHttpRequestor[Asynchronous])
    // This one line changes the whole application from being executed asynchronously to synchronously.
    // val githubService = new HttpGitHubService(new BlockingHttpRequestor[Synchronous])

    Seq("bhuemer", "foobar", "doesthisnameexist", "horst") foreach { user =>
      githubService.repositoryNamesFor(user) map {
        case Some(List())       => println(s"${Thread.currentThread().getName} - Found no repositories for $user.")
        case Some(repositories) => println(s"${Thread.currentThread().getName} - Found repositories: $repositories for $user")
        case None => println(s"${Thread.currentThread().getName} - Could not request repositories for $user.")
      }
    }

    githubService.commitsFor("bhuemer", "scala-playground") map {
      case Some(List())       => println(s"${Thread.currentThread().getName} - Found no commits.")
      case Some(commits) => println(s"${Thread.currentThread().getName} - Found commits: $commits")
      case None => println(s"${Thread.currentThread().getName} - Could not request commits.")
    }

    // In case we're using futures .. wait a bit, don't stop immediately.
    Thread.sleep(20000)
  }

}
