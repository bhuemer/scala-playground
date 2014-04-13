package at.bhuemer.scala.playground.play

sealed trait JsValue
case object JsNull extends JsValue
case class JsBoolean(value: Boolean) extends JsValue
case class JsNumber(value: BigDecimal) extends JsValue
case class JsString(value: String) extends JsValue
case class JsObject(value: Map[String, JsValue]) extends JsValue
