package site.addzero.network.call.browser.windsurf

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitUntilState
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Windsurf 注册页自动填写
 *
 * 两步流程（来自 Playwright Inspector 录制）：
 * 1. firstName + lastName + email + 勾选协议 + 等待 Turnstile 验证 + Continue
 * 2. password + confirmPassword + Continue
 *
 * 使用 persistent context 绕过 Cloudflare Turnstile 人机验证：
 * 浏览器会复用真实 user data 目录，Turnstile 更信任这类指纹。
 */
class WindsurfRegisterAutomation {

  companion object {
    const val REGISTER_URL = "https://windsurf.com/account/register"

    private val DEFAULT_USER_DATA_DIR = Paths.get(
      System.getProperty("java.io.tmpdir"), "playwright-windsurf-profile"
    ).toString()

    /** Turnstile 验证通过后按钮变 enabled 的最大等待时间 */
    private const val TURNSTILE_WAIT_MS = 120_000.0
  }

  fun openAndFill(
    form: WindsurfRegisterForm,
    options: WindsurfRegisterOptions = WindsurfRegisterOptions(),
  ) {
    val playwright = Playwright.create()
    val chromePath = resolveSystemChrome()

    val contextOptions = BrowserType.LaunchPersistentContextOptions()
      .setHeadless(options.automation.headless)
      .setSlowMo(options.automation.slowMoMs)
      .setArgs(listOf(
        "--disable-blink-features=AutomationControlled",
      ))

    chromePath?.let { contextOptions.setExecutablePath(it) }

    val userDataDir = Paths.get(options.userDataDir ?: DEFAULT_USER_DATA_DIR)
    Files.createDirectories(userDataDir)

    val context = playwright.chromium().launchPersistentContext(userDataDir, contextOptions)

    try {
      val page = context.pages().firstOrNull() ?: context.newPage()
      page.setDefaultTimeout(options.automation.timeoutMs)
      page.navigate(
        REGISTER_URL,
        Page.NavigateOptions()
          .setTimeout(options.automation.timeoutMs)
          .setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
      )

      // ── 第一步：基本信息 + 协议 + 等待 Turnstile + Continue ──

      form.firstName?.let {
        page.getByPlaceholder("Your first name").click()
        page.getByPlaceholder("Your first name").fill(it)
      }

      form.lastName?.let {
        page.getByPlaceholder("Your last name").click()
        page.getByPlaceholder("Your last name").fill(it)
      }

      page.getByPlaceholder("Enter your email address").click()
      page.getByPlaceholder("Enter your email address").fill(form.email)

      page.getByLabel("By signing up you agree to").check()

      // 等待 Cloudflare Turnstile 验证通过 → Continue 按钮变 enabled
      // 如果 Turnstile 需要人工介入，会打印提示并轮询等待
      val continueBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue"))
      waitForEnabled(continueBtn, "Continue（第一步）")
      continueBtn.click()

      // ── 第二步：密码 + Continue ──

      page.getByPlaceholder("Create password").click()
      page.getByPlaceholder("Create password").fill(form.password)

      page.getByPlaceholder("Confirm password").click()
      page.getByPlaceholder("Confirm password").fill(form.confirmPassword ?: form.password)

      if (options.autoSubmit) {
        val submitBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue"))
        waitForEnabled(submitBtn, "Continue（第二步）")
        submitBtn.click()
      }

      // 自动化步骤完成，等待用户手动关闭浏览器（方便继续调试后续步骤如邮箱验证码等）
      if (!options.automation.headless) {
        println("[WindsurfRegister] ✅ 自动化填写完成！浏览器保持打开，你可以继续手动操作")
        println("[WindsurfRegister] 💡 手动关闭浏览器窗口后程序将自动退出")
        context.waitForCondition({ context.pages().isEmpty() })
      }

    } finally {
      runCatching { context.close() }
      runCatching { playwright.close() }
    }
  }

  /**
   * 轮询等待按钮变为 enabled（最多 [TURNSTILE_WAIT_MS] 毫秒）
   *
   * Cloudflare Turnstile 验证通过后按钮才会 enabled。
   * 如果是 non-headless 模式，会打印提示让用户手动完成验证。
   */
  private fun waitForEnabled(locator: Locator, label: String) {
    val deadline = System.currentTimeMillis() + TURNSTILE_WAIT_MS.toLong()
    var prompted = false

    while (System.currentTimeMillis() < deadline) {
      if (runCatching { locator.isEnabled }.getOrDefault(false)) {
        if (prompted) println("[WindsurfRegister] ✓ Turnstile 验证通过，继续执行")
        return
      }

      if (!prompted) {
        println("[WindsurfRegister] ⏳ 等待 Cloudflare Turnstile 人机验证通过...")
        println("[WindsurfRegister] 💡 如果浏览器中出现验证挑战，请手动完成，按钮将自动变为可点击")
        prompted = true
      }

      Thread.sleep(1_000)
    }

    error("$label 按钮在 ${TURNSTILE_WAIT_MS.toLong() / 1000}s 内未变为 enabled，Turnstile 验证可能未通过")
  }

  private fun resolveSystemChrome(): java.nio.file.Path? {
    val candidates = listOf(
      "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
      "/usr/bin/google-chrome",
      "/usr/bin/google-chrome-stable",
      "/usr/bin/chromium-browser",
      "/usr/bin/chromium",
      "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
      "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
    )
    return candidates
      .map { Paths.get(it) }
      .firstOrNull { Files.exists(it) }
  }
}
