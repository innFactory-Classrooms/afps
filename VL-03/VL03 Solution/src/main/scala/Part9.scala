object Part9 {

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

    def withFilter(p: A => Boolean): MaybeBox[A] = {
      this match {
        case MaybeBox.Full(value) if p(value) => this
        case _ => MaybeBox.Empty
      }
    }
    
    // Implement fold
    def fold[B](ifEmpty: => B)(f: A => B): B = {
      this match {
        case MaybeBox.Full(value) => f(value)
        case MaybeBox.Empty => ifEmpty
      }
    }
  }


  def part9(): Unit = {

    println("- - - PART 9 - - -")

    val fold1 = MaybeBox.Full(42).fold("leer")(x => s"Wert: $x")
    println(fold1)

    val fold2 = MaybeBox.Empty.fold("leer")(x => s"Wert: $x")
    println(fold2)

  }

}
