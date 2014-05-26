package at.bhuemer.scala.playground.crdts

/**
 *
 */
class TwoPhaseSet[A](private val added: Set[A] = Set.empty[A], private val removed: Set[A] = Set.empty[A]) extends Traversable[A] {

  /**
   * Tests if some element is contained in this set.
   */
  def contains(elem: A): Boolean =
    added.contains(elem) && !removed.contains(elem)

  /**
   * Applies a function `f` to all elements of this set.
   */
  def foreach[U](f: A => U): Unit = added.foreach {
      a => if (!removed.contains(a)) f(a)
    }

  /**
   * Creates a new set with an additional element.
   */
  def +(elem: A): TwoPhaseSet[A] =
    new TwoPhaseSet[A](added + elem, removed)

  /**
   * Creates a new set with the given element removed from this set.
   */
  def -(elem: A): TwoPhaseSet[A] =
    new TwoPhaseSet[A](added, removed + elem)

  def merge(other: TwoPhaseSet[A]): TwoPhaseSet[A] =
    new TwoPhaseSet[A](added ++ other.added, removed ++ other.removed)

}
