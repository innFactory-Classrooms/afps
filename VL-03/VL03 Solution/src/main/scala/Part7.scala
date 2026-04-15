object Part7 {

  enum MaybeBox[+A] {
    case Full(value: A)
    case Empty

    def map[B](f: A => B): MaybeBox[B] = {
      this match {
        case MaybeBox.Full(value) => MaybeBox.Full(f(value))
        case MaybeBox.Empty => MaybeBox.Empty
      }
    }

    def flatMap[B](f: A => MaybeBox[B]): MaybeBox[B] = {
      this match {
        case MaybeBox.Full(value) => f(value)
        case MaybeBox.Empty => MaybeBox.Empty
      }
    }
    
    // Implement withFilter
    def withFilter(p: A => Boolean): MaybeBox[A] = {
      this match {
        case MaybeBox.Full(value) if p(value) => this
        case _ => MaybeBox.Empty
      }
    }
  }

  def part7(): Unit = {

    println("- - - PART 7 - - -")

    for {
      x <- MaybeBox.Full(5)
      if x > 3
    } yield println(s"$x is greater than 3")

     for {
      x <- MaybeBox.Full(2)
      if x > 3
    } yield println(s"$x is greater than 3")

  }

}
