package site.addzero.tool.creditcard

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreditCardGeneratorTest {

  @Test
  fun `should generate valid luhn cards for all types`() {
    CardType.entries.forEach { type ->
      repeat(5) {
        val card = CreditCardGenerator.generate(type)
        val digits = card.cardNumber.filter { it.isDigit() }
        assertTrue(digits.length == 16, "card length should be 16: $digits")
        assertTrue(CreditCardGenerator.luhnValid(digits), "card should pass luhn: $digits")
        assertTrue(type.bins.any { digits.startsWith(it) }, "card should match BIN list: $digits")
      }
    }
  }

  @Test
  fun `should generate batch cards`() {
    val cards = CreditCardGenerator.generateBatch(20)
    assertTrue(cards.size == 20)
    cards.forEach { card ->
      assertTrue(CreditCardGenerator.luhnValid(card.cardNumber), "invalid card: ${card.cardNumber}")
    }
  }
}
