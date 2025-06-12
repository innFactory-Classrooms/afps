package macros

object RunMacro {

//  private val powN3 = mkPower(-3)
  private val pow2 = mkPower(2)

  def main(args: Array[String]): Unit = {
    println(pow2(2))
  }
}
