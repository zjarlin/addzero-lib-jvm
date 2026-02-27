package site.addzero.network.call.browser.windsurf

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

/**
 * Windsurf 注册页各步骤工具类（来自 Playwright Inspector 录制）
 *
 * 每个方法对应注册流程的一个步骤，接收 [Page] 参数，直接操作页面。
 * 方法之间无状态耦合，可以单独调用或组合使用。
 */
object WindsurfSteps {

  /** 按钮 enabled 等待超时（Cloudflare Turnstile） */
  private const val TURNSTILE_WAIT_MS = 120_000L

  // ────────────────────────────────────────────────────────────
  // 第一步：基本信息 + 勾选协议 + Continue
  // ────────────────────────────────────────────────────────────

  /**
   * 填写 firstName、lastName、email，勾选协议，等待 Turnstile 通过后点击 Continue
   */
  fun step1_fillBasicInfoAndContinue(
    page: Page,
    email: String,
    firstName: String? = null,
    lastName: String? = null,
  ) {
    firstName?.let {
      page.getByPlaceholder("Your first name").click()
      page.getByPlaceholder("Your first name").fill(it)
    }

    lastName?.let {
      page.getByPlaceholder("Your last name").click()
      page.getByPlaceholder("Your last name").fill(it)
    }

    println("[WindsurfSteps] step1: expected email=$email")

    val emailInput = page.getByPlaceholder("Enter your email address")
    emailInput.click()
    emailInput.fill(email)

    // 事务内一致性校验：回读输入框实际值，确保与预期邮箱一致
    val actualEmail = emailInput.inputValue()
    println("[WindsurfSteps] step1: actual email in input=$actualEmail")
    check(actualEmail == email) {
      "Email mismatch! expected=$email, actual=$actualEmail — 可能存在浏览器缓存或页面残留"
    }

    page.getByLabel("By signing up you agree to").check()

    val continueBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue"))
    waitForEnabled(continueBtn, "Continue（第一步）", page = page)
    forceClick(continueBtn, "Continue（第一步）")
  }

  // ────────────────────────────────────────────────────────────
  // 第二步：密码 + Continue
  // ────────────────────────────────────────────────────────────

  /**
   * 填写密码和确认密码，等待按钮 enabled 后点击 Continue
   */
  fun step2_fillPasswordAndContinue(
    page: Page,
    password: String,
    confirmPassword: String = password,
  ) {
    println("[WindsurfSteps] step2: current URL=${page.url()}")
    // 等待密码输入框出现，确保页面已从第一步切换过来
    page.getByPlaceholder("Create password").waitFor()
    println("[WindsurfSteps] step2: password page loaded, filling password (password=${password})")

    page.getByPlaceholder("Create password").click()
    page.getByPlaceholder("Create password").fill(password)

    page.getByPlaceholder("Confirm password").click()
    page.getByPlaceholder("Confirm password").fill(confirmPassword)

    // 页面上可能存在多个 Continue 按钮（第一步残影），精确定位第二步的按钮
    val continueBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Continue")).last()
    waitForEnabled(continueBtn, "Continue（第二步）", page = page)
    forceClick(continueBtn, "Continue（第二步）")

    // 等待页面跳转到验证码输入页（出现验证码提示文字或输入框）
    println("[WindsurfSteps] step2: waiting for verification page...")
    runCatching {
      page.waitForURL("**/verify**", Page.WaitForURLOptions().setTimeout(15_000.0))
    }.onFailure {
      // URL 不一定含 verify，退而等待验证码输入框出现
      runCatching { page.locator(".body2").first().waitFor() }
    }
    println("[WindsurfSteps] step2: done, now at URL=${page.url()}")
  }

  // ────────────────────────────────────────────────────────────
  // 第三步：邮箱验证码 + Create account
  // ────────────────────────────────────────────────────────────

  /**
   * 填写 6 位邮箱验证码并点击 Create account
   *
   * @param code 6 位数字验证码
   */
  fun step3_fillVerificationCodeAndSubmit(page: Page, code: String) {
    require(code.length == 6 && code.all { it.isDigit() }) {
      "verificationCode 必须是 6 位数字，实际: $code"
    }
    println("[WindsurfSteps] step3: filling verification code=$code at URL=${page.url()}")

    page.locator(".body2").first().click()
    page.locator(".body2").first().fill(code[0].toString())
    page.locator("input:nth-child(2)").fill(code[1].toString())
    page.locator("input:nth-child(3)").fill(code[2].toString())
    page.locator("input:nth-child(4)").fill(code[3].toString())
    page.locator("input:nth-child(5)").fill(code[4].toString())
    page.locator("input:nth-child(6)").fill(code[5].toString())

    val createBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Create account"))
    waitForEnabled(createBtn, "Create account", page = page)
    forceClick(createBtn, "Create account")
  }

  // ────────────────────────────────────────────────────────────
  // 辅助：等待按钮 enabled（Turnstile 验证）
  // ────────────────────────────────────────────────────────────

  /**
   * 轮询等待 locator 变为 enabled
   */
  fun waitForEnabled(locator: Locator, label: String, timeoutMs: Long = TURNSTILE_WAIT_MS, page: Page? = null) {
    val deadline = System.currentTimeMillis() + timeoutMs
    var prompted = false
    var turnstileAttempted = false

    while (System.currentTimeMillis() < deadline) {
      if (runCatching { locator.isEnabled }.getOrDefault(false)) {
        if (prompted) println("[WindsurfSteps] $label enabled ✓")
        return
      }

      if (!prompted) {
        println("[WindsurfSteps] waiting for $label to become enabled (Turnstile)...")
        println("[WindsurfSteps] >>> 如果看到 'Please verify that you are human'，正在尝试自动点击...")
        prompted = true
      }

      // 每 3 秒尝试自动点击 Turnstile checkbox
      if (page != null && !turnstileAttempted) {
        turnstileAttempted = tryClickTurnstile(page)
      }

      Thread.sleep(1_000)

      // 每 5 秒重试一次 Turnstile 点击
      if (page != null && (System.currentTimeMillis() % 5000) < 1100) {
        tryClickTurnstile(page)
      }
    }

    error("$label 在 ${timeoutMs / 1000}s 内未变为 enabled，请手动点击 Turnstile 验证框")
  }

  /**
   * 尝试自动点击 Turnstile "确认您是真人" checkbox
   *
   * Turnstile 的 checkbox 结构：`<div id="动态ID"> > div > label > input[type=checkbox]`
   * ID 每次动态生成，所以通过结构匹配。同时尝试 iframe 内部点击作为备选。
   *
   * @return true 如果成功点击
   */
  private fun tryClickTurnstile(page: Page): Boolean {
    return try {
      // 策略1：主页面上的 Turnstile checkbox（label 包裹的 input[type=checkbox]）
      val selectors = listOf(
        "div > label > input[type='checkbox']",           // 你提供的结构 #AOzYg6 > div > label > input
        "label input[type='checkbox']",                    // 更宽泛的 label 内 checkbox
      )
      for (selector in selectors) {
        val checkbox = page.locator(selector)
        val count = runCatching { checkbox.count() }.getOrDefault(0)
        if (count > 0) {
          // 可能有多个 checkbox（包括"同意协议"），找到未选中的那个
          for (i in 0 until count) {
            val el = checkbox.nth(i)
            val checked = runCatching { el.isChecked }.getOrDefault(true)
            if (!checked) {
              // 尝试点击 label（比直接点 input 更可靠）
              val clicked = runCatching {
                el.locator("xpath=ancestor::label").first().click(Locator.ClickOptions().setTimeout(2000.0))
                true
              }.getOrElse {
                // 直接点 input
                runCatching { el.click(Locator.ClickOptions().setTimeout(2000.0)); true }.getOrDefault(false)
              }
              if (clicked) {
                println("[WindsurfSteps] auto-clicked Turnstile checkbox (selector=$selector, index=$i)")
                return true
              }
            }
          }
        }
      }

      // 策略2：通过 JS 直接点击 Turnstile checkbox（绕过 pointer-events 拦截）
      val jsClicked = runCatching {
        page.evaluate("""
          () => {
            const inputs = document.querySelectorAll('div > label > input[type="checkbox"]');
            for (const input of inputs) {
              if (!input.checked) {
                input.click();
                return true;
              }
            }
            return false;
          }
        """)
      }.getOrNull()
      if (jsClicked == true) {
        println("[WindsurfSteps] auto-clicked Turnstile checkbox via JS")
        return true
      }

      // 策略3：iframe 内部 checkbox（备选）
      for (frame in page.frames()) {
        if (frame.url().contains("challenges.cloudflare.com")) {
          val checkbox = frame.locator("input[type='checkbox']")
          if (runCatching { checkbox.count() }.getOrDefault(0) > 0) {
            runCatching { checkbox.first().click(Locator.ClickOptions().setTimeout(2000.0)) }
            println("[WindsurfSteps] auto-clicked Turnstile checkbox in iframe")
            return true
          }
          runCatching { frame.locator("body").click(Locator.ClickOptions().setTimeout(2000.0)) }
          println("[WindsurfSteps] auto-clicked Turnstile iframe body")
          return true
        }
      }

      false
    } catch (_: Throwable) {
      false
    }
  }

  /**
   * 绕过 Cloudflare 反自动化拦截的点击策略：
   * 1. 普通 click（最自然）
   * 2. dispatchEvent("click")（绕过 pointer-events 拦截）
   * 3. JS evaluate click（最暴力，直接调用 DOM .click()）
   */
  private fun forceClick(locator: Locator, label: String) {
    val el = locator.first()

    // 策略1：普通点击
    val clicked = runCatching { el.click(); true }.getOrDefault(false)
    if (clicked) {
      println("[WindsurfSteps] clicked $label (normal)")
      return
    }

    // 策略2：dispatchEvent
    val dispatched = runCatching { el.dispatchEvent("click"); true }.getOrDefault(false)
    if (dispatched) {
      println("[WindsurfSteps] clicked $label (dispatchEvent)")
      return
    }

    // 策略3：JS evaluate
    runCatching {
      el.evaluate("el => el.click()")
      println("[WindsurfSteps] clicked $label (js evaluate)")
    }.onFailure {
      error("forceClick failed for $label: ${it.message}")
    }
  }
}
