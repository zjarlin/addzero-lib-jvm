package site.addzero.network.call.browser.windsurf

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WindsurfRandomNameGeneratorTest {

  @Test
  fun `should generate non blank first and last name`() {
    repeat(50) {
      val first = WindsurfRandomNameGenerator.randomFirstName()
      val last = WindsurfRandomNameGenerator.randomLastName()
      assertTrue(first.isNotBlank())
      assertTrue(last.isNotBlank())
    }
  }

  @Test
  fun `should generate full name pair`() {
    repeat(50) {
      val (first, last) = WindsurfRandomNameGenerator.randomFullName()
      assertTrue(first.isNotBlank())
      assertTrue(last.isNotBlank())
    }
  }
}
