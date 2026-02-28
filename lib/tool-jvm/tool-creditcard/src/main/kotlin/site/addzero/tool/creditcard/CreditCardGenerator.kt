package site.addzero.tool.creditcard

import kotlin.random.Random

/**
 * Credit card type with BIN prefixes.
 */
enum class CardType(val bins: List<String>) {
  VISA(listOf("4532", "4539", "4556", "4916", "4929", "4485", "4716")),
  MASTERCARD(listOf("5123", "5234", "5452", "5567", "5187", "5289", "5445")),
  DISCOVER(listOf("6011", "6441", "6442", "6443", "6444", "6445", "6446", "6447", "6448", "6449")),
  JCB(listOf("3528", "3529", "3530", "3531", "3532", "3533", "3534", "3535")),
  ;

  companion object {
    private val random = Random.Default
    fun random(): CardType = entries[random.nextInt(entries.size)]
  }
}

data class BillingAddress(
  val country: String,
  val province: String,
  val city: String,
  val district: String,
  val postalCode: String,
  val addressLine1: String,
  val addressLine2: String? = null,
)

data class UserProfile(
  val firstName: String,
  val lastName: String,
) {
  val fullName: String get() = "$firstName $lastName"
}

data class CreditCardInfo(
  val cardNumber: String,
  val expiry: String, // MM / YY
  val cvc: String,
  val type: CardType,
  val holderName: String,
  val address: BillingAddress,
)

/**
 * Credit card info generator for testing and UI automation.
 *
 * Example:
 * ```kotlin
 * val card = CreditCardGenerator.generate(CardType.VISA)
 * println(CreditCardGenerator.toPipeFormat(card))
 * ```
 *
 * NOTE: generated numbers are Luhn-valid only, not chargeable real cards.
 */
object CreditCardGenerator {
  private val random = Random.Default

  private val firstNames = listOf(
    "James", "Robert", "John", "Michael", "David", "William", "Mary", "Jennifer", "Linda", "Sarah",
  )
  private val lastNames = listOf(
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Wilson", "Taylor", "Lee",
  )

  private val cnAddresses = listOf(
    BillingAddress("CN", "北京市", "北京市", "朝阳区", "100000", "建国路88号", "SOHO现代城"),
    BillingAddress("CN", "上海市", "上海市", "浦东新区", "200000", "陆家嘴环路1000号", null),
    BillingAddress("CN", "广东省", "深圳市", "南山区", "518000", "科技园南路1号", "生态园"),
    BillingAddress("CN", "浙江省", "杭州市", "西湖区", "310000", "文三路477号", null),
  )

  fun generate(type: CardType? = null): CreditCardInfo {
    val cardType = type ?: CardType.random()
    val number = generateCardNumber(cardType)
    val expiry = generateExpiry()
    val cvc = "%03d".format(random.nextInt(100, 1000))
    val profile = UserProfile(firstNames.random(random), lastNames.random(random))
    val address = cnAddresses.random(random)
    return CreditCardInfo(
      cardNumber = formatCardNumber(number),
      expiry = expiry,
      cvc = cvc,
      type = cardType,
      holderName = profile.fullName,
      address = address,
    )
  }

  fun generateBatch(count: Int, type: CardType? = null): List<CreditCardInfo> =
    (1..count).map { generate(type) }

  fun luhnValid(number: String): Boolean {
    val digits = number.filter { it.isDigit() }
    var sum = 0
    var alternate = false
    for (i in digits.length - 1 downTo 0) {
      var n = digits[i] - '0'
      if (alternate) {
        n *= 2
        if (n > 9) n -= 9
      }
      sum += n
      alternate = !alternate
    }
    return sum % 10 == 0
  }

  /** Format: number|MM|20YY|CVV */
  fun toPipeFormat(card: CreditCardInfo): String {
    val (month, year) = card.expiry.split(" / ")
    return "${card.cardNumber.filter { it.isDigit() }}|$month|20$year|${card.cvc}"
  }

  private fun generateCardNumber(type: CardType): String {
    val bin = type.bins.random(random)
    val partial = buildString {
      append(bin)
      while (length < 15) append(random.nextInt(0, 10))
    }
    return partial + luhnCheckDigit(partial)
  }

  private fun luhnCheckDigit(partial: String): Int {
    var sum = 0
    var isEven = false
    for (i in partial.length - 1 downTo 0) {
      var digit = partial[i] - '0'
      if (isEven) {
        digit *= 2
        if (digit > 9) digit -= 9
      }
      sum += digit
      isEven = !isEven
    }
    return (10 - (sum % 10)) % 10
  }

  private fun generateExpiry(): String {
    val month = random.nextInt(1, 13)
    val year = (java.time.Year.now().value % 100) + random.nextInt(1, 6)
    return "%02d / %02d".format(month, year)
  }

  private fun formatCardNumber(number: String): String = number.chunked(4).joinToString(" ")
}
