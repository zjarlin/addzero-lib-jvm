package site.addzero.network.call.browser.windsurf

import kotlin.random.Random

/**
 * Windsurf Pro 绑卡所需的支付信息
 *
 * @param cardNumber   银行卡号（Luhn 校验有效）
 * @param expiry       有效期，格式 "MM / YY"
 * @param cvc          安全码（3~4 位）
 * @param holderName   持卡人姓名
 * @param country      国家代码，如 "CN"
 * @param postalCode   邮编
 * @param province     省/州
 * @param district     区/县
 * @param addressLine1 地址第 1 行
 * @param addressLine2 地址第 2 行（可选）
 */
data class WindsurfCardInfo(
  val cardNumber: String,
  val expiry: String,
  val cvc: String,
  val holderName: String,
  val country: String = "CN",
  val postalCode: String = "100000",
  val province: String = "北京市",
  val district: String = "朝阳区",
  val addressLine1: String = "建国路88号",
  val addressLine2: String? = null,
)

/**
 * 银行卡信息生成器
 *
 * 生成的卡号通过 Luhn 校验，但不是真实卡号，仅用于测试绑卡流程。
 * 支持 Visa (4xxx) 和 Mastercard (5xxx) 前缀。
 */
object WindsurfCardGenerator {

  private val random = Random.Default

  // 中国城市地址库，随机选取
  private val CN_ADDRESSES = listOf(
    CnAddress("北京市", "100000", "朝阳区", "建国路88号", "SOHO现代城"),
    CnAddress("北京市", "100000", "海淀区", "中关村大街1号", "海龙大厦"),
    CnAddress("上海市", "200000", "浦东新区", "陆家嘴环路1000号", "恒生银行大厦"),
    CnAddress("上海市", "200000", "徐汇区", "漕溪北路88号", "圣爱大厦"),
    CnAddress("广东省", "510000", "天河区", "天河路385号", "太古汇"),
    CnAddress("广东省", "518000", "南山区", "科技园南路1号", "深圳湾科技生态园"),
    CnAddress("浙江省", "310000", "西湖区", "文三路477号", "华星科技大厦"),
    CnAddress("江苏省", "210000", "鼓楼区", "中山路88号", "金陵饭店"),
    CnAddress("四川省", "610000", "武侯区", "人民南路四段1号", "来福士广场"),
    CnAddress("湖北省", "430000", "武昌区", "东湖路169号", "楚天传媒大厦"),
  )

  // 英文姓名库
  private val FIRST_NAMES = listOf(
    "James", "Robert", "John", "Michael", "David", "William", "Richard", "Joseph",
    "Thomas", "Charles", "Mary", "Patricia", "Jennifer", "Linda", "Barbara", "Elizabeth",
    "Susan", "Jessica", "Sarah", "Karen", "Alex", "Chris", "Sam", "Taylor", "Jordan",
  )
  private val LAST_NAMES = listOf(
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Anderson", "Taylor", "Thomas", "Jackson", "White", "Harris",
    "Martin", "Thompson", "Moore", "Lee", "Clark", "Lewis", "Robinson", "Walker", "Young",
  )

  /**
   * 生成一套随机的绑卡信息
   *
   * @param cardType 卡类型，"visa" 或 "mastercard"，null 时随机
   */
  fun generate(cardType: String? = null): WindsurfCardInfo {
    val type = cardType ?: if (random.nextBoolean()) "visa" else "mastercard"
    val cardNumber = generateCardNumber(type)
    val expiry = generateExpiry()
    val cvc = generateCvc()
    val name = "${FIRST_NAMES.random(random)} ${LAST_NAMES.random(random)}"
    val addr = CN_ADDRESSES.random(random)

    return WindsurfCardInfo(
      cardNumber = formatCardNumber(cardNumber),
      expiry = expiry,
      cvc = cvc,
      holderName = name,
      country = "CN",
      postalCode = addr.postalCode,
      province = addr.province,
      district = addr.district,
      addressLine1 = addr.addressLine1,
      addressLine2 = addr.addressLine2,
    )
  }

  /**
   * 生成通过 Luhn 校验的卡号
   */
  private fun generateCardNumber(type: String): String {
    val prefix = when (type.lowercase()) {
      "visa" -> "4"
      "mastercard" -> "5${random.nextInt(1, 6)}"
      else -> "4"
    }

    // 生成除最后一位外的数字
    val partialLength = 15 - prefix.length // 总长 16，减去前缀和校验位
    val partial = prefix + (1..partialLength).map { random.nextInt(0, 10) }.joinToString("")

    // Luhn 校验位
    val checkDigit = luhnCheckDigit(partial)
    return partial + checkDigit
  }

  /**
   * Luhn 算法计算校验位
   */
  private fun luhnCheckDigit(partial: String): Int {
    var sum = 0
    // 从右到左（partial 的最后一位是奇数位），需要对偶数位 * 2
    for (i in partial.indices.reversed()) {
      var digit = partial[i] - '0'
      // partial 长度为奇数位时，索引 0 是偶数位（从右数第 partial.length 位）
      val positionFromRight = partial.length - i // 1-indexed from right
      if (positionFromRight % 2 == 0) {
        digit *= 2
        if (digit > 9) digit -= 9
      }
      sum += digit
    }
    // 校验位使 (sum + checkDigit) % 10 == 0
    // 但这里 checkDigit 是在最右边（positionFromRight = 1，奇数位，不翻倍）
    return (10 - (sum % 10)) % 10
  }

  /**
   * 格式化卡号为 "1234 1234 1234 1234"
   */
  private fun formatCardNumber(number: String): String =
    number.chunked(4).joinToString(" ")

  /**
   * 生成未来的有效期 "MM / YY"
   */
  private fun generateExpiry(): String {
    val month = random.nextInt(1, 13)
    // 当前年份 + 1~5 年
    val currentYear = java.time.Year.now().value % 100
    val year = currentYear + random.nextInt(1, 6)
    return "%02d / %02d".format(month, year)
  }

  /**
   * 生成 3 位 CVC
   */
  private fun generateCvc(): String =
    "%03d".format(random.nextInt(100, 1000))

  private data class CnAddress(
    val province: String,
    val postalCode: String,
    val district: String,
    val addressLine1: String,
    val addressLine2: String?,
  )
}
