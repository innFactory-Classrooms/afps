package de.innfactory.afps

import io.github.iltotore.iron.autoRefine
import de.innfactory.afps.RefinedEmail.{Email, validateEmail}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RefinedEmailSpec extends AnyWordSpec with Matchers {

  "validateEmail" when {
    "using runtime validation" should {
      "return Some for valid emails" in {
        validateEmail("test@example.com") shouldBe Some("test@example.com")
        validateEmail("user.name+tag@domain.co.uk") shouldBe Some("user.name+tag@domain.co.uk")
      }

      "return None for invalid emails" in {
        validateEmail("invalid-email") shouldBe None
        validateEmail("user@com") shouldBe None
        validateEmail("@example.com") shouldBe None
      }
    }

    "using compile time validation" should {
      "accept valid emails" in {
        val validEmail: Email = "test@example.com"
        validEmail shouldBe "test@example.com"
      }

      "reject invalid emails" in {
        """val invalidEmail: Email = "invalid-email"""" shouldNot compile
        """val invalidEmail: Email = "user@com"""" shouldNot compile
        """val invalidEmail: Email = "@example.com"""" shouldNot compile
      }
    }
  }
}