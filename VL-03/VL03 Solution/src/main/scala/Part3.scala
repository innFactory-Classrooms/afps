import Part2.Box

object Part3 {

  def part3(): Unit = {

    println("- - - PART 3 - - -")
    
    for {
      a <- Box(5)
      b <- Box(a * 2)
    } yield println(b + a)
    
  }

  
}
