import Part2.Box

object Part4 {

  val f: Int => Box[Int] = x => Box(x + 1)
  val g: Int => Box[Int] = x => Box(x * 2)
  val m = Box(5)
  
  def part4(): Unit = {
    
    println("- - - PART 4 - - -")
    
    val leftIdentity = Box(5).flatMap(f) == f(5)
    val rightIdentity = m.flatMap(Box(_)) == m
    val associativity = m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))
    
    println(s"LeftIdentity: $leftIdentity")
    println(s"RightIdentity: $rightIdentity")
    println(s"Associativity: $associativity")

    assert(leftIdentity)
    assert(rightIdentity)
    assert(associativity)
    
    println("asserts are valid")
  }

}
