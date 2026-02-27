package site.addzero.network.call.browser.core

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Frame
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import com.microsoft.playwright.options.WaitUntilState
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 通用浏览器表单自动填写引擎
 *
 * 使用方式：
 * ```kotlin
 * BrowserFormFiller.fill(
 *   url = "https://example.com/register",
 *   fields = listOf(
 *     FormFieldDef("email", listOf("input[name='email']"), "a@b.com", required = true),
 *     FormFieldDef("submit", listOf("button[type='submit']"), type = FieldType.CLICK),
 *   ),
 *   options = BrowserAutomationOptions(headless = false, debug = true),
 * )
 * ```
 */
object BrowserFormFiller {

  /**
   * 多步骤表单填写（适用于需要点击"下一步"才出现后续字段的场景）
   *
   * @param url    目标页面 URL
   * @param steps  每一步是一组 [FormFieldDef]，步骤之间会等待页面稳定
   * @param options 浏览器启动与调试配置
   */
  fun fillSteps(
    url: String,
    steps: List<List<FormFieldDef>>,
    options: BrowserAutomationOptions = BrowserAutomationOptions(),
  ) {
    withPage(url, options) { page ->
      steps.forEachIndexed { index, fields ->
        if (index > 0) {
          runCatching { page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED) }
          runCatching { Thread.sleep(500) }
        }
        fields.forEach { field ->
          when (field.type) {
            FieldType.INPUT -> handleInput(page, url, options, field)
            FieldType.CLICK -> handleClick(page, url, options, field)
            FieldType.CHECK -> handleCheck(page, url, options, field)
          }
        }
      }
    }
  }

  /**
   * 打开页面并按 [fields] 列表依次填写/点击
   *
   * @param url              目标页面 URL
   * @param fields           字段定义列表（按顺序执行）
   * @param options          浏览器启动与调试配置
   * @param submitSelectors  可选的提交按钮 selector 列表（填完所有字段后点击）
   * @return 填写完成后的 [Page]（已在 lambda 内，资源会在返回后自动关闭）
   */
  fun fill(
    url: String,
    fields: List<FormFieldDef>,
    options: BrowserAutomationOptions = BrowserAutomationOptions(),
    submitSelectors: List<String>? = null,
  ) {
    withPage(url, options) { page ->
      fields.forEach { field ->
        when (field.type) {
          FieldType.INPUT -> handleInput(page, url, options, field)
          FieldType.CLICK -> handleClick(page, url, options, field)
          FieldType.CHECK -> handleCheck(page, url, options, field)
        }
      }

      submitSelectors?.let { clickAny(page, it) }
    }
  }

  /**
   * 打开页面并在 [block] 中自由操作 [Page]，结束后自动关闭浏览器
   */
  fun <T> withPage(
    url: String,
    options: BrowserAutomationOptions = BrowserAutomationOptions(),
    block: (Page) -> T,
  ): T {
    val playwright = Playwright.create()
    val launchOptions = BrowserType.LaunchOptions()
      .setHeadless(options.headless)
      .setSlowMo(options.slowMoMs)

    resolveSystemChrome()?.let { launchOptions.setExecutablePath(it) }

    val browser = playwright.chromium().launch(launchOptions)

    return try {
      val page = browser.newPage()
      page.setDefaultTimeout(options.timeoutMs)
      page.navigate(
        url,
        Page.NavigateOptions()
          .setTimeout(options.timeoutMs)
          .setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
      )
      runCatching { page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE) }

      if (options.debug && !options.headless) {
        runCatching { page.pause() }
      }

      block(page)
    } finally {
      runCatching { browser.close() }
      runCatching { playwright.close() }
    }
  }

  // ── 内部：字段处理 ──────────────────────────────────────────

  private fun handleInput(page: Page, url: String, options: BrowserAutomationOptions, field: FormFieldDef) {
    if (field.value.isBlank() && !field.required) return

    val filled = tryFillAcrossFrames(page, field.selectors, field.value)
    if (!filled && field.required) {
      debugDump(page, options, field.name)
      error("Cannot find ${field.name} field on $url")
    }
  }

  private fun handleClick(page: Page, url: String, options: BrowserAutomationOptions, field: FormFieldDef) {
    val clicked = clickAny(page, field.selectors)
    if (!clicked && field.required) {
      debugDump(page, options, field.name)
      error("Cannot find ${field.name} button on $url")
    }
  }

  private fun handleCheck(page: Page, url: String, options: BrowserAutomationOptions, field: FormFieldDef) {
    val checked = checkAny(page, field.selectors)
    if (!checked && field.required) {
      debugDump(page, options, field.name)
      error("Cannot find ${field.name} checkbox on $url")
    }
  }

  // ── 内部：填写逻辑（三级 fallback + 跨 iframe）──────────────

  private fun tryFillAcrossFrames(page: Page, selectors: List<String>, value: String): Boolean {
    if (tryFillInPage(page, selectors, value)) return true

    page.frames().forEach { frame ->
      if (frame != page.mainFrame()) {
        if (tryFillInFrame(frame, selectors, value)) return true
      }
    }
    return false
  }

  private fun tryFillInPage(page: Page, selectors: List<String>, value: String): Boolean {
    selectors.forEach { sel ->
      if (tryFillLocator(resolveLocator(page, sel), value)) return true
    }
    return false
  }

  private fun tryFillInFrame(frame: Frame, selectors: List<String>, value: String): Boolean {
    selectors.forEach { sel ->
      if (tryFillLocator(frame.locator(sel), value)) return true
    }
    return false
  }

  /**
   * 解析 selector 字符串，支持：
   * - `"label:xxx"` → `page.getByLabel("xxx")`
   * - `"placeholder:xxx"` → `page.getByPlaceholder("xxx")`
   * - `"role:button:Continue"` → `page.getByRole(BUTTON, name="Continue")`
   * - 其他 → `page.locator(selector)`（CSS / Playwright 选择器）
   */
  private fun resolveLocator(page: Page, selector: String): Locator {
    return when {
      selector.startsWith("label:") ->
        page.getByLabel(selector.removePrefix("label:"))
      selector.startsWith("placeholder:") ->
        page.getByPlaceholder(selector.removePrefix("placeholder:"))
      selector.startsWith("role:") -> {
        val parts = selector.removePrefix("role:").split(":", limit = 2)
        val role = AriaRole.valueOf(parts[0].uppercase())
        if (parts.size > 1) {
          page.getByRole(role, Page.GetByRoleOptions().setName(parts[1]))
        } else {
          page.getByRole(role)
        }
      }
      else -> page.locator(selector)
    }
  }

  private fun tryFillLocator(locator: Locator, value: String): Boolean {
    if (locator.count() <= 0) return false

    val first = locator.first()
    val visible = runCatching {
      first.waitFor(
        Locator.WaitForOptions()
          .setState(WaitForSelectorState.VISIBLE)
          .setTimeout(2_000.0),
      )
      true
    }.getOrElse { runCatching { first.isVisible }.getOrDefault(false) }

    if (!visible) return false

    // 策略1：click + fill
    runCatching {
      first.click()
      first.fill(value)
      return true
    }

    // 策略2：clear + fill
    runCatching {
      first.clear()
      first.fill(value)
      return true
    }

    // 策略3：clear + type（模拟键盘逐字输入）
    runCatching {
      first.clear()
      first.type(value)
      return true
    }

    return false
  }

  // ── 内部：点击逻辑 ─────────────────────────────────────────

  private fun clickAny(page: Page, selectors: List<String>): Boolean {
    selectors.forEach { sel ->
      val locator = resolveLocator(page, sel)
      if (locator.count() > 0 && locator.first().isVisible) {
        locator.first().click()
        return true
      }
    }
    return false
  }

  private fun checkAny(page: Page, selectors: List<String>): Boolean {
    selectors.forEach { sel ->
      val locator = resolveLocator(page, sel)
      if (locator.count() > 0) {
        runCatching {
          locator.first().check()
          return true
        }
      }
    }
    return false
  }

  // ── 内部：调试产物输出 ─────────────────────────────────────

  /**
   * 当 [BrowserAutomationOptions.debug] 为 true 时，输出：
   * - 截图（`*-<fieldName>.png`）
   * - 页面 HTML（`*-<fieldName>.html`）
   * - 所有 input 元素属性（`*-<fieldName>-inputs.json`）
   */
  private fun debugDump(page: Page, options: BrowserAutomationOptions, fieldName: String) {
    if (!options.debug) return

    val dir: Path = runCatching {
      options.artifactsDir?.let { Paths.get(it) }
        ?: Paths.get(System.getProperty("java.io.tmpdir"), "playwright-artifacts")
    }.getOrElse {
      Paths.get(System.getProperty("java.io.tmpdir"))
    }

    runCatching { Files.createDirectories(dir) }

    val ts = java.time.LocalDateTime.now()
      .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
    val base = "$ts-$fieldName"

    // 截图
    runCatching {
      page.screenshot(
        Page.ScreenshotOptions()
          .setPath(dir.resolve("$base.png"))
          .setFullPage(true),
      )
    }

    // HTML
    runCatching {
      val html = page.content()
      Files.newBufferedWriter(dir.resolve("$base.html")).use { it.write(html) }
    }

    // 所有 input 元素属性 JSON
    runCatching {
      @Suppress("MaxLineLength")
      val json = page.evaluate(
        """() => JSON.stringify(Array.from(document.querySelectorAll('input, select, textarea, button')).map(el => ({
          tag: el.tagName.toLowerCase(),
          type: el.getAttribute('type'),
          name: el.getAttribute('name'),
          id: el.getAttribute('id'),
          placeholder: el.getAttribute('placeholder'),
          autocomplete: el.getAttribute('autocomplete'),
          ariaLabel: el.getAttribute('aria-label'),
          dataTestId: el.getAttribute('data-testid'),
          className: el.className,
          visible: el.offsetParent !== null
        })), null, 2)"""
      ) as String
      Files.newBufferedWriter(dir.resolve("$base-inputs.json")).use { it.write(json) }
    }

    println("[BrowserFormFiller] Debug artifacts saved to: $dir/$base*")
  }

  // ── 内部：系统 Chrome 检测 ─────────────────────────────────

  private fun resolveSystemChrome(): Path? {
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
