package de.innfactory.afps

object Expressions {
  val meaningOfLife = 40 + 2 * 3 / 3 // bitwise |, &, <<, >>, >>>

  val equalityTest = 1 == 2 // <, <=, >, >=, ==, !=

  val nonEqualityTest = !equalityTest

  // Alles in Scala ist ein Ausdruck
  val anIfExpressions = if (1 < 2) {
    2
  } else if (1 < 3) 3 else "test"

  val codeBlocks = {
    val a = {
      32 + 32
    }
    a
  }

  val someValue = {
    2 < 3
  }
  val someOtherValue = {
    if(someValue) 239 else 986
    42
  }
  val yetAnotherValue: Unit = println("Scala")
  val aUnit = ()

  val aNothing = throw new IllegalArgumentException()

  def main(args: Array[String]): Unit = {
    println(someValue) // false
    println(someOtherValue) // 42
    println(yetAnotherValue) // Scala oder Unit/Nichts
  }
}
