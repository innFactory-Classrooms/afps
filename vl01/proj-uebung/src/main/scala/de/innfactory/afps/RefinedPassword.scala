package de.innfactory.afps

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

object RefinedPassword {
  type PasswordConstraint = MinLength[6] & Exists[LowerCase] & Exists[UpperCase] & Exists[Digit]
  type Password = String :| PasswordConstraint

  def validatePassword(value: String): Option[String] = value.refineOption[PasswordConstraint]
}
