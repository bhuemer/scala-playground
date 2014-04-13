package at.bhuemer.scala.playground.play

case class Person(firstName: String, lastName: String, age: Int)

object Person {

  import Formats.stringsFormats
  import Formats.intsFormats

  implicit val personFormats: Reads[Person] = Formats(
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

}