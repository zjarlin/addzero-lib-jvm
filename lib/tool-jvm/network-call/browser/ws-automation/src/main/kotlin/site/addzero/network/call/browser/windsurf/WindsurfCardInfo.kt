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
 * @param city         城市
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
  val city: String = "北京市",
  val district: String = "朝阳区",
  val addressLine1: String = "建国路88号",
  val addressLine2: String? = null,
)

/**
 * 卡类型枚举
 *
 * 参考 [furyolo/creditcardcrack](https://github.com/furyolo/creditcardcrack)
 * 每种类型定义了真实发卡行 BIN 前缀（Bank Identification Number）
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

/**
 * 银行卡信息生成器
 *
 * 参考 [furyolo/creditcardcrack](https://github.com/furyolo/creditcardcrack)
 * 使用真实发卡行 BIN 前缀 + Luhn 校验位生成格式有效的卡号。
 * 支持 Visa / MasterCard / Discover / JCB 四种卡类型。
 *
 * ⚠ 生成的卡号仅通过 Luhn 校验，不是可扣款的真实卡号。
 */
object WindsurfCardGenerator {

  private val random = Random.Default

  // ────────── BIN 前缀库（来自 creditcardcrack） ──────────

  // ────────── 中国城市地址库 ──────────

  private val CN_ADDRESSES = listOf(
    CnAddress("北京市", "100000", "北京市", "朝阳区", "建国路88号", "SOHO现代城"),
    CnAddress("北京市", "100000", "北京市", "海淀区", "中关村大街1号", "海龙大厦"),
    CnAddress("北京市", "100000", "北京市", "丰台区", "六里桥南路1号", null),
    CnAddress("上海市", "200000", "上海市", "浦东新区", "陆家嘴环路1000号", "恒生银行大厦"),
    CnAddress("上海市", "200000", "上海市", "徐汇区", "漕溪北路88号", "圣爱大厦"),
    CnAddress("广东省", "510000", "广州市", "天河区", "天河路385号", "太古汇"),
    CnAddress("广东省", "518000", "深圳市", "南山区", "科技园南路1号", "深圳湾科技生态园"),
    CnAddress("浙江省", "310000", "杭州市", "西湖区", "文三路477号", "华星科技大厦"),
    CnAddress("江苏省", "210000", "南京市", "鼓楼区", "中山路88号", "金陵饭店"),
    CnAddress("四川省", "610000", "成都市", "武侯区", "人民南路四段1号", "来福士广场"),
    CnAddress("湖北省", "430000", "武汉市", "武昌区", "东湖路169号", "楚天传媒大厦"),
    CnAddress("陕西省", "710000", "西安市", "雁塔区", "科技路48号", "创业广场"),
    CnAddress("辽宁省", "110000", "沈阳市", "和平区", "青年大街286号", "华润大厦"),
    CnAddress("福建省", "350000", "福州市", "鼓楼区", "五四路89号", "恒力城"),
    CnAddress("山东省", "250000", "济南市", "历下区", "泉城路26号", "银座商城"),
  )

  // ────────── 英文姓名库 ──────────

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

  // ────────── 公开 API ──────────

  /**
   * 生成一套随机的绑卡信息
   *
   * @param cardType 卡类型，null 时从 Visa/MC/Discover/JCB 中随机
   */
  fun generate(cardType: CardType? = null): WindsurfCardInfo {
    val type = cardType ?: CardType.random()
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
      city = addr.city,
      district = addr.district,
      addressLine1 = addr.addressLine1,
      addressLine2 = addr.addressLine2,
    )
  }

  /**
   * 批量生成卡信息
   *
   * @param count    数量
   * @param cardType 指定卡类型，null 时每张随机
   */
  fun generateBatch(count: Int, cardType: CardType? = null): List<WindsurfCardInfo> =
    (1..count).map { generate(cardType) }

  /**
   * 格式化为 `卡号|月份|年份|CVV` 格式（兼容 creditcardcrack 格式）
   */
  fun formatPipe(card: WindsurfCardInfo): String {
    val digits = card.cardNumber.replace(" ", "")
    val (month, year) = card.expiry.split(" / ")
    return "$digits|$month|20$year|${card.cvc}"
  }

  /**
   * Luhn 校验
   */
  fun luhnValid(number: String): Boolean {
    val digits = number.replace(" ", "")
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

  // ────────── 内部实现 ──────────

  /**
   * 基于 BIN 前缀生成通过 Luhn 校验的 16 位卡号
   */
  private fun generateCardNumber(type: CardType): String {
    val bin = type.bins.random(random)

    // 生成 BIN 后的随机数字（保留最后一位给校验位）
    val partial = buildString {
      append(bin)
      while (length < 15) append(random.nextInt(0, 10))
    }

    val checkDigit = luhnCheckDigit(partial)
    return partial + checkDigit
  }

  /**
   * Luhn 算法计算校验位
   */
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
    val city: String,
    val district: String,
    val addressLine1: String,
    val addressLine2: String?,
  )
}
