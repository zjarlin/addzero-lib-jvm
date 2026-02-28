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

  /** Continue 按钮的可能文本（英文 + Chrome 翻译后的中文） */
  private val CONTINUE_NAMES = listOf("Continue", "继续", "下一步")
  /** Create account 按钮的可能文本 */
  private val CREATE_ACCOUNT_NAMES = listOf("Create account", "创建帐户", "创建账户", "注册")

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
    // 先关闭所有 Chrome 原生弹框（恢复页面、密码保存、翻译栏等）
    dismissChromeDialogs(page)

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

    val continueBtn = findButtonByNames(page, CONTINUE_NAMES)
      ?: error("[WindsurfSteps] step1: Continue button not found (tried: $CONTINUE_NAMES)")
    waitForEnabled(continueBtn, "Continue（第一步）", page = page)
    dismissChromeDialogs(page)
    forceClick(continueBtn, "Continue（第一步）")

    // 等待页面跳转到密码页（密码输入框出现 或 URL 变化 或 被重定向到 profile）
    println("[WindsurfSteps] step1: waiting for page transition after Continue...")
    val step1Deadline = System.currentTimeMillis() + 30_000L
    var step1Transitioned = false
    while (System.currentTimeMillis() < step1Deadline) {
      val currentUrl = runCatching { page.url() }.getOrDefault("")
      // 信号1：密码输入框出现
      val passwordVisible = runCatching {
        page.getByPlaceholder("Create password").isVisible
      }.getOrDefault(false)
      if (passwordVisible) {
        println("[WindsurfSteps] step1: password page appeared")
        step1Transitioned = true
        break
      }
      // 信号2：被重定向到 profile/dashboard（已注册过）
      if ("profile" in currentUrl || "dashboard" in currentUrl) {
        println("[WindsurfSteps] step1: redirected to $currentUrl (already registered)")
        step1Transitioned = true
        break
      }
      // 信号3：检测到页面错误提示
      val errorText = runCatching {
        val errEl = page.locator("[role='alert'], .error-message, .text-red-500, .text-destructive").first()
        if (errEl.isVisible) errEl.textContent()?.trim() else null
      }.getOrNull()
      if (!errorText.isNullOrBlank()) {
        error("[WindsurfSteps] step1: registration error: $errorText (email=$email)")
      }
      Thread.sleep(1_000)
    }
    if (!step1Transitioned) {
      val finalUrl = runCatching { page.url() }.getOrDefault("unknown")
      val screenshotPath = runCatching {
        val tmpFile = java.io.File.createTempFile("step1-timeout-", ".png")
        page.screenshot(Page.ScreenshotOptions().setPath(tmpFile.toPath()).setFullPage(true))
        tmpFile.absolutePath
      }.getOrNull()
      error("[WindsurfSteps] step1: TIMEOUT (30s) waiting for password page. url=$finalUrl, screenshot=$screenshotPath")
    }
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
    // 先关闭所有 Chrome 原生弹框（恢复页面、密码保存等），否则会遮挡页面导致 waitFor 超时
    dismissChromeDialogs(page)
    // 等待密码输入框出现，确保页面已从第一步切换过来
    try {
      page.getByPlaceholder("Create password").waitFor(
        Locator.WaitForOptions().setTimeout(30_000.0)
      )
    } catch (e: Exception) {
      val currentUrl = runCatching { page.url() }.getOrDefault("unknown")
      val screenshotPath = runCatching {
        val tmpFile = java.io.File.createTempFile("step2-waitpwd-", ".png")
        page.screenshot(Page.ScreenshotOptions().setPath(tmpFile.toPath()).setFullPage(true))
        tmpFile.absolutePath
      }.getOrNull()
      val pageTitle = runCatching { page.title() }.getOrDefault("unknown")
      error("[WindsurfSteps] step2: 'Create password' input not found within 30s. url=$currentUrl, title=$pageTitle, screenshot=$screenshotPath")
    }
    println("[WindsurfSteps] step2: password page loaded, filling password (password=${password})")

    page.getByPlaceholder("Create password").click()
    page.getByPlaceholder("Create password").fill(password)

    page.getByPlaceholder("Confirm password").click()
    page.getByPlaceholder("Confirm password").fill(confirmPassword)

    // 点击 Continue（可能需要点 1~2 次，第一次可能触发 Turnstile，第二次才提交）
    val continueBtn1 = findButtonByNames(page, CONTINUE_NAMES, pickLast = true)
      ?: error("[WindsurfSteps] step2: Continue button not found (tried: $CONTINUE_NAMES)")
    waitForEnabled(continueBtn1, "Continue（第二步-第1次点击）", page = page)
    dismissChromeDialogs(page)
    forceClick(continueBtn1, "Continue（第二步-第1次点击）")

    // 等 2 秒观察页面是否已跳转，如果还在密码页则尝试第二次点击
    Thread.sleep(2_000)
    val urlAfterFirstClick = runCatching { page.url() }.getOrDefault("")
    val stillOnPasswordPage = "register" in urlAfterFirstClick && "verify" !in urlAfterFirstClick
      && runCatching { page.getByPlaceholder("Create password").isVisible }.getOrDefault(false)

    if (stillOnPasswordPage) {
      println("[WindsurfSteps] step2: still on password page after 1st click, attempting 2nd Continue click...")
      val continueBtn2 = findButtonByNames(page, CONTINUE_NAMES, pickLast = true)
      if (continueBtn2 != null) {
        waitForEnabled(continueBtn2, "Continue（第二步-第2次点击）", page = page)
        dismissChromeDialogs(page)
        forceClick(continueBtn2, "Continue（第二步-第2次点击）")
      }
    } else {
      println("[WindsurfSteps] step2: page already transitioned after 1st click (url=$urlAfterFirstClick), skipping 2nd click")
    }

    // 等待页面跳转：轮询多种信号（验证码页、profile 重定向、错误提示）
    println("[WindsurfSteps] step2: waiting for next page after Continue...")
    val step2Deadline = System.currentTimeMillis() + 30_000L
    var step2Resolved = false
    var step2PollCount = 0
    while (System.currentTimeMillis() < step2Deadline) {
      step2PollCount++
      val currentUrl = runCatching { page.url() }.getOrDefault("")

      // 每 5 秒打印一次当前 URL，方便诊断
      if (step2PollCount % 5 == 1) {
        println("[WindsurfSteps] step2: polling... url=$currentUrl")
      }

      // 信号1：已跳转到验证码页
      if ("verify" in currentUrl) {
        println("[WindsurfSteps] step2: arrived at verification page: $currentUrl")
        step2Resolved = true
        break
      }

      // 信号2：已跳转到 profile/dashboard（邮箱已注册过，自动登录）
      if ("profile" in currentUrl || "dashboard" in currentUrl) {
        println("[WindsurfSteps] step2: redirected to $currentUrl (already registered)")
        step2Resolved = true
        break
      }

      // 信号3：验证码输入框已出现（URL 可能不含 verify）
      val codeInputVisible = runCatching {
        page.locator(".body2").first().isVisible
      }.getOrDefault(false)
      if (codeInputVisible) {
        println("[WindsurfSteps] step2: verification code input detected on current page")
        step2Resolved = true
        break
      }

      // 信号4：页面显示错误信息（邮箱已存在等）
      val errorText = runCatching {
        val errEl = page.locator("[role='alert'], .error-message, .text-red-500, .text-destructive").first()
        if (errEl.isVisible) errEl.textContent()?.trim() else null
      }.getOrNull()
      if (!errorText.isNullOrBlank()) {
        println("[WindsurfSteps] step2: page shows error: $errorText")
        step2Resolved = true
        break
      }

      // Continue 之后可能出现新的 Turnstile 挑战或 Chrome 弹框阻止跳转
      if (step2PollCount % 3 == 0) {
        dismissChromeDialogs(page)
        tryClickTurnstile(page)
      }

      Thread.sleep(1_000)
    }

    if (!step2Resolved) {
      val finalUrl = runCatching { page.url() }.getOrDefault("unknown")
      // 截图辅助调试
      val screenshotPath = runCatching {
        val tmpFile = java.io.File.createTempFile("step2-timeout-", ".png")
        page.screenshot(Page.ScreenshotOptions().setPath(tmpFile.toPath()).setFullPage(true))
        tmpFile.absolutePath
      }.getOrNull()
      error("[WindsurfSteps] step2: TIMEOUT (30s) waiting for page transition. url=$finalUrl, screenshot=$screenshotPath")
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

    // 等待验证码输入框出现
    page.locator(".body2").first().waitFor()
    page.locator(".body2").first().click()
    page.locator(".body2").first().fill(code[0].toString())
    page.locator("input:nth-child(2)").fill(code[1].toString())
    page.locator("input:nth-child(3)").fill(code[2].toString())
    page.locator("input:nth-child(4)").fill(code[3].toString())
    page.locator("input:nth-child(5)").fill(code[4].toString())
    page.locator("input:nth-child(6)").fill(code[5].toString())

    val createBtn = findButtonByNames(page, CREATE_ACCOUNT_NAMES)
      ?: error("[WindsurfSteps] step3: Create account button not found (tried: $CREATE_ACCOUNT_NAMES)")
    waitForEnabled(createBtn, "Create account", page = page)
    dismissChromeDialogs(page)
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
   * 关闭 Chrome 原生弹框（密码保存、翻译栏、自动填充等）
   *
   * Chrome 的“要保存密码吗？”弹框和 Google Translate 工具栏都是浏览器原生 UI，
   * 会遮挡页面元素或翻译按钮文本导致自动化失败。
   * 通过发送 Escape 键关闭所有弹框。
   */
  private fun dismissChromeDialogs(page: Page) {
    runCatching {
      page.keyboard().press("Escape")
      Thread.sleep(200)
    }
  }

  /**
   * 在多个可能的按钮名称中查找第一个存在的按钮。
   * 用于处理 Chrome 翻译把“Continue”翻译成“继续”等场景。
   *
   * @param names    候选按钮名称列表（先匹配先返回）
   * @param pickLast 是否取最后一个匹配（用于 step2 存在多个同名按钮时取最后一个）
   */
  private fun findButtonByNames(page: Page, names: List<String>, pickLast: Boolean = false): Locator? {
    for (name in names) {
      val btn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName(name))
      val count = runCatching { btn.count() }.getOrDefault(0)
      if (count > 0) {
        println("[WindsurfSteps] found button '$name' (count=$count, pickLast=$pickLast)")
        return if (pickLast) btn.last() else btn.first()
      }
    }
    // 兆底：CSSSelector 直接找所有 button
    val allButtons = page.locator("button")
    val count = runCatching { allButtons.count() }.getOrDefault(0)
    if (count > 0) {
      println("[WindsurfSteps] button name match failed (tried: $names), falling back to CSS button selector (found $count buttons)")
      return if (pickLast) allButtons.last() else allButtons.first()
    }
    return null
  }

  /**
   * 绕过 Cloudflare 反自动化拦截的点击策略：
   * 1. 先尝试关闭 Chrome 原生弹框
   * 2. 普通 click（最自然）
   * 3. dispatchEvent("click")（绕过 pointer-events 拦截）
   * 4. JS evaluate click（最暴力，直接调用 DOM .click()）
   */
  private fun forceClick(locator: Locator, label: String) {
    // 先关闭可能存在的 Chrome 原生弹框（密码保存等）
    runCatching {
      locator.page().keyboard().press("Escape")
      Thread.sleep(200)
    }
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
