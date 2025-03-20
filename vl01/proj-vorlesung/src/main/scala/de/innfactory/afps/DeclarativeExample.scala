object DeclarativeExample {
  def main(args: Array[String]): Unit = {
    val numbers = List(1,2,3,4,5,6);
    val evenNumbersSquared = numbers
      .filter(_ % 2 == 0)
      .map(n => n * n)
    println(evenNumbersSquared)
  }
}