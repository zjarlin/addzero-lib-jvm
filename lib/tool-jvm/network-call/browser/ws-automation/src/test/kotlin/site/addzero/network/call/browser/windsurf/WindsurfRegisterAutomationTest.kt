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
   * 使用前先在终端启动 Chrome：
   * /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --remote-debugging-port=9222 --user-data-dir=/tmp/chrome-cdp-profile
   */
  private val defaultOptions = WindsurfRegisterOptions(
    automation = BrowserAutomationOptions(
      cdpUrl = "http://localhost:9222",
      timeoutMs = 60_000.0,
      slowMoMs = 500.0,
    ),
  )

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
      password = "StrongPass123!",
      firstName = "Auto",
      lastName = "User",
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
   * 仅测试卡号生成器
   */
  @Test
  fun `should generate valid card info`() {
    repeat(5) {
      val card = WindsurfCardGenerator.generate()
      println("[Test] generated card: number=${card.cardNumber}, expiry=${card.expiry}, cvc=${card.cvc}, name=${card.holderName}")
      println("[Test]   address: ${card.province} ${card.district} ${card.addressLine1} ${card.addressLine2 ?: ""}")

      // 验证卡号格式
      val digits = card.cardNumber.replace(" ", "")
      assertTrue(digits.length == 16, "card number should be 16 digits: $digits")
      assertTrue(luhnCheck(digits), "card number should pass Luhn check: $digits")
      assertTrue(card.cvc.length == 3, "CVC should be 3 digits")
      assertTrue(card.expiry.matches(Regex("\\d{2} / \\d{2}")), "expiry should be MM / YY format")
    }
  }

  /**
   * 批量注册测试（串行）
   *
   * 只需指定数量，全自动完成：创建邮箱 → 注册 → 绑卡
   * 改 count 即可控制注册数量，改 concurrency 即可并发
   *
   * 使用前先在终端启动 Chrome：
   * /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --remote-debugging-port=9222 --user-data-dir=/tmp/chrome-cdp-profile
   */
  @Test
  fun `should batch register accounts`() {
    val result = WindsurfBatchRegistration.run(
      count = 1,          // 先跑 1 个验证单流程，跑通后改大
      concurrency = 1,    // 串行，跑通后可改为 2~3 并发
      bindCard = true,
      mailProviderFactory = { TempMailProviderImpl() },
    )

    assertTrue(result.success > 0, "at least one account should succeed")
    println("[Test] batch result: ${result.success}/${result.total} success, ${result.failed} failed")
  }

  private fun luhnCheck(number: String): Boolean {
    var sum = 0
    var alternate = false
    for (i in number.length - 1 downTo 0) {
      var n = number[i] - '0'
      if (alternate) {
        n *= 2
        if (n > 9) n -= 9
      }
      sum += n
      alternate = !alternate
    }
    return sum % 10 == 0
  }
}
