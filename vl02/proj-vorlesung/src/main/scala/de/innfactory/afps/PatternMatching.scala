package de.innfactory.afps

object PatternMatching {
  def classifyNumber(n: Int): String = n match {
    case 0               => "null"
    case 1 | 2           => "1 oder 2"
    case _ if n % 2 == 0 => "gerade zahl"
    case _ if n < 0      => "negativ"
    case _               => "irgendwas anderes"
  }

  def classifyType(n: Any): String = n match {
    case _: String       => s"String $n"
    case x: Int if x < 0 => s"Zahl $n"
  }

  def tupleMatch(n: (Option[String], Option[Int])): String = {
    val returnValue = n match {
      case (Some(a), Some(b)) => s"$a, $b"
      case (Some(a), _)       => s"String: $a, int egal"
      case (_, Some(b))       => s"String egal, int: $b"
      case (_, _)             => "beides egal"
    }
    returnValue
  }

  sealed trait MultipleChoice
  class ChoiceA extends MultipleChoice
  class ChoiceB extends MultipleChoice

  def choiceMatch(c: MultipleChoice) = c match {
    case a: ChoiceA => s"user chose $a"
    case _: ChoiceB => s"user chose"
  }
}
