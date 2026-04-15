object Part2 {

  case class Box[A](value: A) {

    def map[B](f: A => B): Box[B] = {
      Box(f(value))
    }

    // Implement flatMap
    def flatMap[B](f: A => Box[B]): Box[B] = {
      f(value)
    }
  }

}
