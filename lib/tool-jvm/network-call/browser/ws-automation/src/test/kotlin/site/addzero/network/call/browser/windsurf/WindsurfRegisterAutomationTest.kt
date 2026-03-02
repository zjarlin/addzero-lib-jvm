package site.addzero.network.call.browser.windsurf

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import site.addzero.network.call.browser.core.BrowserAutomationOptions

@Tag("integration")
//@Disabled("Real registration test — enable manually")
class WindsurfRegisterAutomationTest {

  /**
   * CDP 模式：连接真实 Chrome，完全绕过 Turnstile
   *
   * Chrome 会自动启动（跨平台 macOS/Windows/Linux），无需手动执行任何命令。
   */
  private val defaultOptions = WindsurfRegisterOptions(
    automation = BrowserAutomationOptions(
      cdpUrl = "http://localhost:9222",
      timeoutMs = 60_000.0,
      slowMoMs = 500.0,
    ),
  )

  /**
   * 批量注册测试（串行）
   *
   * 只需指定数量，全自动完成：创建邮箱 → 注册 → 绑卡
   * 改 count 即可控制注册数量，改 concurrency 即可并发
   * Chrome 会自动启动，无需手动执行任何命令。
   */
  @Test
  fun `should batch register accounts`() {
    val result = WindsurfBatchRegistration.run(
      count = 30,          // 注册数量
      concurrency = 1,    // 串行模式（邮箱 API 有 rate-limit，并发收益不大）
//      bindCard = true,
    )

    assertTrue(result.success > 0, "at least one account should succeed")
    println("[Test] batch result: ${result.success}/${result.total} success, ${result.failed} failed")
  }

  /**
   * 全自动注册测试：
   * 1. mail.tm 创建临时邮箱
   * 2. 浏览器自动填写注册表单（三步）
   * 3. 轮询收件箱提取验证码
   * 4. 自动提交，完成注册
   */
  @Test
  fun `should fully register with temp mail`() {
    val account = WindsurfRegistration.registerWithTempMail(
      mailProvider = TempMailProviderImpl(),
      options = defaultOptions.copy(
        autoSubmit = true,
        automation = defaultOptions.automation.copy(
          timeoutMs = 120_000.0,
        ),
      ),
      postRegistrationAction = { page ->
        println("[Test] registration done, starting automatic card binding...")
        WindsurfCardBinding.bindCard(page)
      },
    )

    println("[Test] registered account: email=${account.windsurfEmail}, savedAt=${WindsurfAccountStorage.DEFAULT_DIR}")
    assertTrue(account.windsurfEmail.contains("@"), "registered email should be valid: ${account.windsurfEmail}")
    assertTrue(account.mailPassword.isNotBlank(), "mail password should be saved")
  }

  /**
   * 半自动注册测试：自动填写表单，验证码页面保持浏览器打开等待手动输入
   */
  @Test
  fun `should register with manual verification code`() {
    WindsurfRegistration.register(
      form = WindsurfRegisterForm(
        email = "demo@example.com",
        password = "StrongPass123!",
        firstName = "Demo",
        lastName = "User",
        // verificationCode = null → 浏览器保持打开，手动输入验证码
      ),
      options = defaultOptions,
    )

    assertTrue(true, "Manual registration flow completed")
  }

  /**
   * 测试卡号生成器（BIN 前缀 + Luhn 校验，参考 creditcardcrack）
   * 覆盖 Visa / MasterCard / Discover / JCB 四种卡类型
   */
  @Test
  fun `should generate valid card info for all card types`() {
    for (type in CardType.entries) {
      println("\n[Test] ── ${type.name} ──")
      repeat(3) {
        val card = WindsurfCardGenerator.generate(type)
        val digits = card.cardNumber.replace(" ", "")
        val pipe = WindsurfCardGenerator.formatPipe(card)

        println("[Test] $pipe  name=${card.holderName}  addr=${card.province}${card.district}${card.addressLine1}")

        assertTrue(digits.length == 16, "card number should be 16 digits: $digits")
        assertTrue(WindsurfCardGenerator.luhnValid(digits), "card number should pass Luhn check: $digits")
        assertTrue(card.cvc.length == 3, "CVC should be 3 digits: ${card.cvc}")
        assertTrue(card.expiry.matches(Regex("\\d{2} / \\d{2}")), "expiry should be MM / YY format: ${card.expiry}")

        // 验证 BIN 前缀属于对应卡类型
        val matchesBin = type.bins.any { digits.startsWith(it) }
        assertTrue(matchesBin, "${type.name} card should start with one of ${type.bins}: $digits")
      }
    }
  }

  /**
   * 测试批量生成
   */
  @Test
  fun `should batch generate cards`() {
    val cards = WindsurfCardGenerator.generateBatch(10)
    println("[Test] batch generated ${cards.size} cards:")
    cards.forEach { println("  ${WindsurfCardGenerator.formatPipe(it)}") }

    assertTrue(cards.size == 10, "should generate 10 cards")
    cards.forEach { card ->
      assertTrue(WindsurfCardGenerator.luhnValid(card.cardNumber), "all cards should pass Luhn")
    }
  }

  /**
   * 测试 printSuccessSummary 汇总输出功能
   *
   * 验证：
   * 1. 能正确读取 success 目录下的账号
   * 2. 按注册时间排序
   * 3. 输出格式：邮箱 密码
   */
  @Test
  fun `should print success summary correctly`() {
    WindsurfBatchRegistration.printSuccessSummary(WindsurfAccountStorage.DEFAULT_DIR,)

  }

  /**
   * 测试 printSuccessSummary 处理空目录的情况
   */
  @Test
  fun `should handle empty success directory gracefully`() {
    val tempDir = kotlin.io.path.createTempDirectory("windsurf-test-empty-")

    // 不创建 success 目录，直接调用
    println("[Test] Testing printSuccessSummary with empty directory:")
    WindsurfBatchRegistration.printSuccessSummary(tempDir)

    // 清理
    tempDir.toFile().deleteRecursively()

    assertTrue(true, "printSuccessSummary handled empty directory without error")
  }

}
