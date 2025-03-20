package de.innfactory.afps

object ScalaExpressions {
  def main(args: Array[String]): Unit = {
    val ergebnis = if (10 > 5) "Größer" else "Kleiner"

    val blockErgebnis = {
      val x = 10;
      val y = 20;
      x + y
    }
  }
}