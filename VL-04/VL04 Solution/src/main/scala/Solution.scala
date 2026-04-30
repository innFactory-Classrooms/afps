import scala.annotation.tailrec

object Solution {

  case class Student(name: String, grade: Double)

  @main
  def solution(): Unit = {

    trait JsonEncoder[A] {
      def encode(v: A): String
    }

    given JsonEncoder[String] with {
      override def encode(v: String): String = s"\"$v\""
    }

    given JsonEncoder[Int] with {
      override def encode(v: Int): String = v.toString
    }

    given JsonEncoder[Boolean] with {
      override def encode(v: Boolean): String = v.toString
    }

    given JsonEncoder[Double] with {
      override def encode(v: Double): String = v.toString
    }

    given JsonEncoder[Float] with {
      override def encode(v: Float): String = v.toString
    }


    given JsonEncoder[Student] with {
      override def encode(v: Student): String = {
        val stringEncoder = summon[JsonEncoder[String]]
        val doubleEncoder = summon[JsonEncoder[Double]]

        s" { \"name\": ${stringEncoder.encode(v.name)}, \"grade\": ${doubleEncoder.encode(v.grade)} } "
      }
    }

    given JsonEncoder[Product] with {
      override def encode(v: Product): String = {

        val elementNames = v.productElementNames
        val values = v.productIterator
        val elementNamesWithValues = elementNames.zip(values)

        def encodeNameAndValue[A: JsonEncoder](v: String, n: A) = {
          val jsonEncoderA: JsonEncoder[A] = summon[JsonEncoder[A]]
          s"\"$v\": ${jsonEncoderA.encode(n)}"
        }

        val stringEncoder = summon[JsonEncoder[String]]

        elementNamesWithValues.map {
          case (str: String, v: Double) => {
            val encoder = summon[JsonEncoder[Double]]
            stringEncoder.encode(str) + ":" + encoder.encode(v)
          }
          case (str: String, v: String) => {
            stringEncoder.encode(str) + ":" + stringEncoder.encode(v)
          }
          case (str: String, v: Int) => encodeNameAndValue(str, v)
          case (str: String, v: Float) => encodeNameAndValue(str, v)
        }.mkString("{ ", ", ", " }")
      }
    }

    // Teil 3
    given [A](using enc: JsonEncoder[A]): JsonEncoder[List[A]] with {
      def encode(values: List[A]): String =
        values.map(enc.encode).mkString("[ ", ", ", " ]")
    }

    extension [A](a: A) {
      def encode(using je: JsonEncoder[A]) = {
        je.encode(a)
      }
    }


    def debugJson(s: Student): String = {
      given JsonEncoder[Student] with {
        override def encode(v: Student): String = s"STUDENT ${v.name} with grade ${v.grade}"
      }
      s.encode
    }

    val hasPassedPartialFunction: PartialFunction[Double, Boolean] = {
      case x if x <= 4.0 => true
    }


    val hasPassedStrPartialFunction: PartialFunction[Double, String] = {
      case x if x <= 4.0 => "bestanden"
    }

    val grades = List(1.3, 2.0, 4.0, 5.0)

    println(grades.collect(hasPassedStrPartialFunction))
    println(grades.collect(hasPassedPartialFunction))
    println(grades.collect {
      case x if x <= 5 => "hat note"
    })

    @tailrec
    def sumList(l: List[Int], acc: Int): Int = {
      l match {
        case head :: tail => sumList(tail, acc + head)
        case Nil => acc
      }
    }

    def multiply(factor: Int)(value: Int) = factor * value

    // Beides Funktioniert
    def double(value: Int) = multiply(2)
    val doubleVal = multiply(2)

    double(5)
    doubleVal(5)

    val students = List(
      Student("Alice", 1.3),
      Student("Bob", 4.0),
      Student("Charlie", 5.0)
    )

    println(
      summon[JsonEncoder[List[String]]].encode(
        students.map(_.grade).collect(hasPassedStrPartialFunction)
      )
    )

    println(
      summon[JsonEncoder[List[Student]]].encode(
        students.filter(_.grade <= 4.0)
      )
    )


  }


}
