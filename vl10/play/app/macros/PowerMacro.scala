package macros

import scala.quoted.{Expr, Quotes}

inline def mkPower(n: Int): Double => Double =
  ${ mkPowerImpl('n) }

private def mkPowerImpl(n: Expr[Int])(using q: Quotes): Expr[Double => Double] = {
  import q.reflect.*
  n.value match {
    case Some(0) => '{ (x: Double) => 1.0 }
    case Some(1) => '{ (x: Double) => x }
    case Some(k) if k > 1 =>
      val prev = mkPowerImpl(Expr(k - 1))
      '{ (x: Double) => x * ${ prev }.apply(x) }
    case _ =>
      report.error("Power must be a non-negative constant integer")
      '{ (x: Double) => Double.NaN }
  }
}
