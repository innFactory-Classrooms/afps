import Part5.MaybeBox
import Part5.MaybeBox.Full

object Part8 {
  

  def part8(): Unit = {

    println("- - - PART 8 - - -")

    case class Student(name: String, grade: Double)

    val students = Map(1 -> Student("Alice", 1.3), 2 -> Student("Bob", 5.0))

    def findStudent(id: Int): MaybeBox[Student] = {
      students.get(1) match {
        case Some(value) => MaybeBox.Full(value)
        case None => MaybeBox.Empty
      }
    }

    def checkPassed(s: Student): MaybeBox[Student] = {
      if (s.grade < 4) MaybeBox.Full(s)
      else MaybeBox.Empty
    }

    val result = for {
      student <- findStudent(1)
      passed <- checkPassed(student)
    } yield s"${passed.name} bestanden mit ${passed.grade}"

    println(result)

  }

}
