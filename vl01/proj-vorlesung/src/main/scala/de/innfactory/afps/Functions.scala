package de.innfactory.afps

object Functions {
  def aFunction(a: String, b: String) = s"$a-$b"

  val aFunctionVal = (a: String, b: String) => s"$a-$b"

  val aFunctionInvocation1 = aFunction("a", "b")
  val aFunctionInvocation2 = aFunctionVal("a", "b")

  def aNoArgFunc(): Int = 42

  def aNoArgFuncSimple: Int = 42

  def stringConcatenation(str: String, n: Int): String =
    if (n <= 0) ""
    else if (n == 1) str
    else s"$str${stringConcatenation(str, n - 1)}"
}
