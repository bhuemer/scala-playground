package at.bhuemer.scala.playground.play

trait Reads[A] {
  def read(value: JsValue): Option[A]
}

trait Writes[A] {
  def write(value: A): JsValue
}

/** One trait to rule them all */
trait Formats[A] extends Reads[A] with Writes[A]

/** Defines instances for those built-in types */
object Formats {

  /** So that we don't have to implement the error case all the time and to get rid of other boiler-plate code */
  def apply[A](readF: PartialFunction[JsValue, Option[A]], writeF: A => JsValue) = new Formats[A] {
    override def read(value: JsValue): Option[A] = readF.applyOrElse(value, _ => None)
    override def write(value: A): JsValue = writeF.apply(value)
  }

  implicit val intsFormats: Formats[Int] = Formats({ case JsNumber(d) => Some(d.toInt) }, JsNumber(_))
  implicit val floatsFormats: Formats[Float] = Formats({ case JsNumber(d) => Some(d.toFloat) }, JsNumber(_))
  implicit val doublesFormats: Formats[Double] = Formats({ case JsNumber(d) => Some(d.toDouble) }, JsNumber(_))
  implicit val booleanFormats: Formats[Boolean] = Formats({ case JsBoolean(b) => Some(b) }, JsBoolean)
  implicit val stringsFormats: Formats[String] = Formats({ case JsString(s) => Some(s) }, JsString)

}
