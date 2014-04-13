package at.bhuemer.scala.playground.play

trait Reads[A] {
  def read(value: JsValue): Option[A]
}

trait Writes[A] {
  def write(value: Option[A]): JsValue
}

/** One trait to rule them all */
trait Formats[A] extends Reads[A] with Writes[A]

/** Defines instances for those built-in types */
object Formats {

  /** So that we don't have to implement the error case all the time and to get rid of other boiler-plate code */
  def apply[A](readF: PartialFunction[JsValue, Option[A]], writeF: A => JsValue) = new Formats[A] {
    override def read(value: JsValue): Option[A] = if (readF.isDefinedAt(value)) readF.apply(value) else None
    override def write(value: Option[A]): JsValue = value.map(writeF).getOrElse(JsNull)
  }

  implicit val intsFormats: Formats[Int] = Formats({ case JsNumber(d) => Some(d.toInt) }, JsNumber(_))
  implicit val floatsFormats: Formats[Float] = Formats({ case JsNumber(d) => Some(d.toFloat) }, JsNumber(_))
  implicit val doublesFormats: Formats[Double] = Formats({ case JsNumber(d) => Some(d.toDouble) }, JsNumber(_))
  implicit val booleanFormats: Formats[Boolean] = Formats({ case JsBoolean(b) => Some(b) }, JsBoolean)
  implicit val stringsFormats: Formats[String] = Formats({ case JsString(s) => Some(s) }, JsString)

}

trait FormatsBuilderOps {
  def read[A : Reads]: ReadsBuilder[A]
}

object FormatsBuilderOps {

  implicit def stringFormatBuilderOps(fieldName: String) = new FormatsBuilderOps {
    override def read[A: Reads]: ReadsBuilder[A] = new ReadsBuilder[A] {
      override def build: Reads[A] = new Reads[A] {
        override def read(value: JsValue): Option[A] = value match {
          case JsObject(fields) => implicitly[Reads[A]].read(fields(fieldName))
          case _ => None
        }
      }
    }
  }

}

trait ReadsBuilder[A] {
  def build: Reads[A]
}

object ReadsBuilder {

  implicit def readsReadsBuilder[A](reads: Reads[A]) = new ReadsBuilder[A] {
    override def build: Reads[A] = reads
  }

  implicit def readsBuilderApplicative = new scalaz.Apply[ReadsBuilder] {
    override def map[A, B](fa: ReadsBuilder[A])(f: A => B): ReadsBuilder[B] = new ReadsBuilder[B] {
      override def build: Reads[B] = new Reads[B] {
        override def read(value: JsValue): Option[B] = fa.build.read(value) map f
      }
    }

    override def ap[A, B](fa: => ReadsBuilder[A])(fab: => ReadsBuilder[A => B]): ReadsBuilder[B] = new ReadsBuilder[B] {
      override def build: Reads[B] = new Reads[B] {
        override def read(value: JsValue): Option[B] = for {
          a <- fa.build.read(value)
          f <- fab.build.read(value)
        } yield f(a)
      }
    }
  }

}