package de.innfactory.afps

import de.innfactory.afps.RefinedPassword.{Password, validatePassword}
import io.github.iltotore.iron.autoRefine
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RefinedPasswordSpec extends AnyWordSpec with Matchers {

  "validatePassword" when {
    "using runtime validation" should {
      "return Some for valid passwords" in {
        validatePassword("Passw0rd") shouldBe Some("Passw0rd")
        validatePassword("A1b2c3d4") shouldBe Some("A1b2c3d4")
      }

      "return None for passwords that are too short" in {
        validatePassword("Pw0") shouldBe None
      }

      "return None for passwords without digits" in {
        validatePassword("Password") shouldBe None
      }

      "return None for passwords without uppercase letters" in {
        validatePassword("password1") shouldBe None
      }

      "return None for passwords without lowercase letters" in {
        validatePassword("PASSWORD1") shouldBe None
      }
    }

    "using compile time validation" should {
      "accept valid passwords" in {
        val validPassword: Password = "Passw0rd"
        validPassword shouldBe "Passw0rd"
      }

      "reject passwords that are too short" in {
        """val shortPassword: Password = "Pw0""" shouldNot compile
      }

      "reject passwords without digits" in {
        """val noDigitPassword: Password = "Password""" shouldNot compile
      }

      "reject passwords without uppercase letters" in {
        """val noUppercasePassword: Password = "password1""" shouldNot compile
      }

      "reject passwords without lowercase letters" in {
        """val noLowercasePassword: Password = "PASSWORD1""" shouldNot compile
      }
    }
  }
}
