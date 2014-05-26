package at.bhuemer.scala.playground.support

import scala.util.Random

/**
 * Just encapsulates everything we need to simulate non-determinism in this little toy example.
 */
trait NonDeterminism {

  /** To create a fluent API */
  trait Choice { def or(g: => Unit) }

  /** Will either execute `f` .. */
  def either(f: => Unit) = new Choice {
    /** .. or `g` randomly. */
    override def or(g: => Unit): Unit =
      if (nextBoolean()) f else g
  }

  /** Will execute `f` many times, potentially, but at least once. */
  def atLeastOnce(f: => Unit): Unit =
    either {
      f; atLeastOnce(f)
    } or {
      f
    }

  /**
   * In this method we control how random we want things to behave. For example,
   * as it stands only in 20% of all cases will we introduce some random behaviour.
   */
  private val rnd = new Random()
  def nextBoolean() = rnd.nextDouble() > 0.8

}
