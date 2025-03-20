package de.innfactory.afps

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

object RefinedEmail {
  private type EmailConstraint = Match["^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"]

  type Email = String :| EmailConstraint

  def validateEmail(value: String): Option[String] = value.refineOption[EmailConstraint]
}
