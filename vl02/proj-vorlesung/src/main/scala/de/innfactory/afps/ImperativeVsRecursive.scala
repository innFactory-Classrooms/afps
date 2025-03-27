package de.innfactory.afps

import scala.annotation.tailrec

object ImperativeVsRecursive {
  def factorialLoop(n: Int): Int = {
    var result = 1
    for (i <- 1 to n) {
      result = result * i
    }
    result
  }

  def factorialRec(n: Int): Int = {
    if (n == 0) 1
    else n * factorialRec(n - 1)
  }

  @tailrec
  def factorialTailrec(n: BigInt, acc: BigInt = 1): BigInt = {
    if (n == 0) acc
    else factorialTailrec(n - 1, n * acc)
  }

  def stringConcatenation(str: String, n: Int): String = {
    @tailrec
    def loop(str: String, n: Int, acc: String = ""): String = {
      if (n <= 0) acc
      else loop(str, n - 1, s"$acc$str")
    }
    loop(str, n)
  }

  def main(args: Array[String]): Unit = {
    // println(factorialTailrec(100))
    println(stringConcatenation("test", 5))
  }
}
