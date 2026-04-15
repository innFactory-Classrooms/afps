object Part1 {

  case class Box[A](value: A) {
    def map[B](f: A => B): Box[B] = {
      Box(f(value))
    }
  }

}
