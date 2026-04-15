import Part2.Box

object Part5 {

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
  }

}
