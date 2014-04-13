package at.bhuemer.scala.playground.play

case class Person(firstName: String, lastName: String, age: Int)

object Person {

  def create = {
    firstName: String => {
      lastName: String => {
        age: Int => apply(firstName, lastName, age)
      }
    }
  }

  import Formats.stringsFormats
  import Formats.intsFormats

  implicit val personFormats: Formats[Person] = Formats(
    readF = {
      case JsObject(fields) => for {
        firstName <- stringsFormats.read(fields("firstName"))
        lastName <- stringsFormats.read(fields("lastName"))
        age <- intsFormats.read(fields("age"))
      } yield Person(firstName, lastName, age) // Only when there's all three attributes, we'll return a person instance
    },
    writeF = {
      person => JsObject(Map(
        "firstName" -> stringsFormats.write(Some(person.firstName)), // we insist that they're not null
        "lastName" -> stringsFormats.write(Some(person.lastName)),
        "age" -> intsFormats.write(Some(person.age))
      ))
    }
  )

  implicit val personReads: Reads[Person] = {
    import scalaz._
    import Scalaz._
    import ReadsBuilder._
    import FormatsBuilderOps._
    import Formats._

    ("firstName".read[String] |@|
    "lastName".read[String] |@|
    "age".read[Int])(Person.apply).build
  }

  def main(args: Array[String]): Unit = {
    println(personReads.read(JsObject(Map(
      "firstName" -> JsString("John"),
      "lastName" -> JsString("Doe"),
      "age" -> JsNumber(12)
    ))))
  }

}