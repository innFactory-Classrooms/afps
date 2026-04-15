import Part5.MaybeBox
import Part5.MaybeBox.Full

object Part6 {

 def saveDivide(a: Int, b: Int): MaybeBox[Int] = {
    if (b == 0) MaybeBox.Empty
    else MaybeBox.Full(a / b)
  }
  
  def part6(): Unit = {
    
    println("- - - PART 6 - - -")
    
    val resultValid = for {
      a <- Full(20)
      b <- saveDivide(a, 4)
      c <- saveDivide(b, 2)
    } yield c


    val resultInvalid = for {
      a <- Full(20)
      b <- saveDivide(a, 4)
      c <- saveDivide(b, 0)
    } yield c
    
    println(resultValid) // Should print: Full(2)
    println(resultInvalid) // Should print: Empty
    
  }

}
