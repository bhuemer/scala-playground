package at.bhuemer.scala.playground.github

import at.bhuemer.scala.playground.github.service.{GitHubService, Commit, HttpGitHubService}
import at.bhuemer.scala.playground.github.http.BlockingHttpRequestor
import at.bhuemer.scala.playground.github.concurrent.{Synchronous, Asynchronous}
import at.bhuemer.scala.playground.github.monad.Monad
import at.bhuemer.scala.playground.github.monad.functions.sequence
import at.bhuemer.scala.playground.github.monad.functions.pure

case class ProjectStats(owner: String, repositoryName: String, commits: List[Commit])

/**
 *
 */
object GitHubDemoApp {

  import at.bhuemer.scala.playground.github.monad.syntax._

  def findProjectStatus[Context[_]: Monad]
      (service: GitHubService[Context], owner: String)(repository: String): Context[ProjectStats] =
    service.commitsFor(owner, repository) map { maybeCommits =>
      ProjectStats(owner, repository, maybeCommits.getOrElse(Nil))
    }

  def findAllProjectStats[Context[_] : Monad]
      (service: GitHubService[Context], owner: String): Context[List[ProjectStats]] =
    service.repositoryNamesFor(owner) flatMap {
      case Some(repositoryNames) =>
        sequence(
          repositoryNames map findProjectStatus(service, owner)
        )
      case None => pure(Nil.asInstanceOf[List[ProjectStats]])
    }

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val githubService = new HttpGitHubService(new BlockingHttpRequestor[Asynchronous])
    // This one line changes the whole application from being executed asynchronously to synchronously.
    // val githubService = new HttpGitHubService(new BlockingHttpRequestor[Synchronous])

//    findAllProjectStats(githubService, "bhuemer") map { allProjectStats =>
//      allProjectStats map { projectStats =>
//        println(s"For the project ${projectStats.repositoryName} by" +
//          s" ${projectStats.owner} we found the following commits: ${projectStats.commits}.")
//      }
//    }

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
