package at.bhuemer.scala.playground.github

import at.bhuemer.scala.playground.github.service.{GitHubService, Commit, HttpGitHubService}
import at.bhuemer.scala.playground.github.http.BlockingHttpRequestor
import at.bhuemer.scala.playground.github.concurrent._
import at.bhuemer.scala.playground.github.monad.Monad
import at.bhuemer.scala.playground.github.monad.functions.sequence

case class ProjectStats(owner: String, repositoryName: String, commits: List[Commit])

/**
 *
 */
object GitHubDemoApp {

  import at.bhuemer.scala.playground.github.monad.syntax._

  def findProjectStatus[Context[_]: Monad]
      (service: GitHubService[Context], owner: String, repositoryName: String): Context[ProjectStats] =
    for {
      commits <- service.commitsFor(owner, repositoryName)
    } yield ProjectStats(owner, repositoryName, commits)

  def findAllProjectStats[Context[_]: Monad]
      (service: GitHubService[Context], owner: String, repositoryNames: List[String]): Context[List[ProjectStats]] =
    sequence(
      for {
        repositoryName <- repositoryNames
      } yield findProjectStatus(service, owner, repositoryName)
    )

  def findAllProjectStats[Context[_] : Monad]
      (service: GitHubService[Context], owner: String): Context[List[ProjectStats]] =
    service.repositoryNamesFor(owner) flatMap {
      repositoryNames =>
        findAllProjectStats(service, owner, repositoryNames)
    }

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val githubService = new HttpGitHubService(new BlockingHttpRequestor[Asynchronous])
    // This one line changes the whole application from being executed asynchronously to synchronously.
    // val githubService = new HttpGitHubService(new BlockingHttpRequestor[Synchronous])

    findAllProjectStats(githubService, "bhuemer") map { allProjectStats =>
      allProjectStats map { projectStats =>
        println(s"For the project ${projectStats.repositoryName} by" +
          s" ${projectStats.owner} we found the following commits: ${projectStats.commits}.")
      }
    }

//    Seq("bhuemer", "foobar", "doesthisnameexist", "horst") foreach { user =>
//      githubService.repositoryNamesFor(user) map {
//        case Some(List())       => println(s"${Thread.currentThread().getName} - Found no repositories for $user.")
//        case Some(repositories) => println(s"${Thread.currentThread().getName} - Found repositories: $repositories for $user")
//        case None => println(s"${Thread.currentThread().getName} - Could not request repositories for $user.")
//      }
//    }
//
    githubService.commitsFor("bhuemer", "scala-playground") map {
      case Nil     => println(s"${Thread.currentThread().getName} - Found no commits.")
      case commits => println(s"${Thread.currentThread().getName} - Found commits: $commits")
    } recover {
      case ex      => println(s"${Thread.currentThread().getName} - Could not request commits, because of $ex")
    }

    githubService.followerNamesFor("foobar23434") map {
      case Nil       => println(s"${Thread.currentThread().getName} - Found no followers.")
      case followers => println(s"${Thread.currentThread().getName} - Found followers: $followers")
    } recover {
      case ex        => println(s"${Thread.currentThread().getName} - Could not find followers, because of $ex")
    }

    // In case we're using futures .. wait a bit, don't stop immediately.
    Thread.sleep(20000)
  }

}
