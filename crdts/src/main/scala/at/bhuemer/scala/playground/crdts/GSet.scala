package at.bhuemer.scala.playground.crdts

/**
 *
 */
class GSet[A](private val elements: Set[A] = Set.empty) extends Traversable[A] {

  /**
   * Tests if some element is contained in this set.
   */
  def contains(elem: A): Boolean =
    elements.contains(elem)

  /**
   * Applies a function `f` to all elements of this set.
   */
  def foreach[U](f: A => U): Unit =
    elements.foreach(f)

  /**
   * Creates a new set with an additional element.
   */
  def +(elem: A): GSet[A] =
    new GSet[A](elements + elem)

  def merge(other: GSet[A]): GSet[A] =
    new GSet[A](elements ++ other.elements)

}
