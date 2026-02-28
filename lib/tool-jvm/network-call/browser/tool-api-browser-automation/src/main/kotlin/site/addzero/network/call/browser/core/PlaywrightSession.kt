package site.addzero.network.call.browser.core

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.WaitUntilState
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 统一浏览器生命周期管理
 *
 * 提供两种模式：
 * - [withPage]：普通匿名 context，适用于一般自动化
 * - [withPersistentPage]：persistent context，复用 profile 目录，适用于需绕过 Cloudflare Turnstile 的场景
 *
 * 两种模式均自动处理：
 * - 系统 Chrome 检测（优先使用系统 Chrome）
 * - 浏览器启动 / 关闭
 * - 反自动化检测参数注入
 */
object PlaywrightSession {

  /**
   * 普通匿名 context：打开页面 → 执行 [block] → 自动关闭
   *
   * @param url     导航目标 URL，为 null 时不自动导航
   * @param options 启动配置
   * @param block   在 page 上执行的业务逻辑
   */
  fun <T> withPage(
    url: String? = null,
    options: BrowserAutomationOptions = BrowserAutomationOptions(),
    block: (Page) -> T,
  ): T {
    val playwright = Playwright.create()
    val launchOptions = BrowserType.LaunchOptions()
      .setHeadless(options.effectiveHeadless)
      .setSlowMo(options.slowMoMs)
      .setArgs(STEALTH_ARGS)

    ChromeResolver.resolve()?.let { launchOptions.setExecutablePath(it) }

    val browser = playwright.chromium().launch(launchOptions)

    return try {
      val page = browser.newPage()
      page.setDefaultTimeout(options.timeoutMs)
      injectStealthScripts(page)

      if (url != null) {
        page.navigate(
          url,
          Page.NavigateOptions()
            .setTimeout(options.timeoutMs)
            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
        )
        runCatching { page.waitForLoadState(LoadState.NETWORKIDLE) }
      }

      block(page)
    } finally {
      runCatching { browser.close() }
      runCatching { playwright.close() }
    }
  }

  /**
   * Persistent context：复用 profile 目录，打开页面 → 执行 [block] → 自动关闭
   *
   * 启动前自动清理 `SingletonLock`，避免上次进程未正常退出时启动失败。
   * 当 [options] 中设置了 `cdpUrl` 时，自动切换为 CDP 模式连接真实 Chrome。
   *
   * @param url        导航目标 URL，为 null 时不自动导航
   * @param profileDir profile 目录（不存在时自动创建）
   * @param options    启动配置
   * @param block      在 page + context 上执行的业务逻辑
   */
  fun <T> withPersistentPage(
    url: String? = null,
    profileDir: Path = defaultProfileDir(),
    options: BrowserAutomationOptions = BrowserAutomationOptions(),
    block: (page: Page, context: BrowserContext) -> T,
  ): T {
    // CDP 模式：连接真实 Chrome，完全绕过 Turnstile
    if (options.cdpUrl != null) {
      return withCdpPage(url = url, options = options, block = block)
    }

    Files.createDirectories(profileDir)
    cleanSingletonLock(profileDir)

    val playwright = Playwright.create()
    val contextOptions = BrowserType.LaunchPersistentContextOptions()
      .setHeadless(options.effectiveHeadless)
      .setSlowMo(options.slowMoMs)
      .setArgs(STEALTH_ARGS)

    ChromeResolver.resolve()?.let { contextOptions.setExecutablePath(it) }

    val context = playwright.chromium().launchPersistentContext(profileDir, contextOptions)

    return try {
      val page = context.pages().firstOrNull() ?: context.newPage()
      page.setDefaultTimeout(options.timeoutMs)
      injectStealthScripts(page)

      if (url != null) {
        page.navigate(
          url,
          Page.NavigateOptions()
            .setTimeout(options.timeoutMs)
            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
        )
      }

      block(page, context)
    } finally {
      runCatching { context.close() }
      runCatching { playwright.close() }
    }
  }

  /**
   * CDP 模式：连接真实 Chrome 浏览器（自动启动）
   *
   * 完全绕过 Cloudflare Turnstile 等反自动化检测，因为浏览器本身不是 Playwright 启动的。
   * 如果指定端口没有 Chrome 在监听，会自动启动 Chrome 并开启 CDP 端口。
   *
   * @param url     导航目标 URL
   * @param options 配置（需设置 cdpUrl，如 `http://localhost:9222`）
   * @param block   在 page + context 上执行的业务逻辑
   */
  fun <T> withCdpPage(
    url: String? = null,
    options: BrowserAutomationOptions = BrowserAutomationOptions(),
    block: (page: Page, context: BrowserContext) -> T,
  ): T {
    val rawCdpUrl = options.cdpUrl
      ?: error("cdpUrl must be set for CDP mode")

    // 从 cdpUrl 提取端口号，自动确保 Chrome 在该端口运行
    val port = rawCdpUrl
      .substringAfterLast(":")
      .trimEnd('/')
      .toIntOrNull() ?: 9222
    val cdpUrl = ChromeLauncher.ensureRunning(port = port)

    println("[PlaywrightSession] connecting to Chrome via CDP: $cdpUrl")
    val playwright = Playwright.create()
    val browser = playwright.chromium().connectOverCDP(cdpUrl)

    var page: Page? = null
    return try {
      val context = browser.contexts().firstOrNull()
        ?: error("No browser context found. Is Chrome running with --remote-debugging-port?")
      page = context.newPage()
      page.setDefaultTimeout(options.timeoutMs)

      if (url != null) {
        page.navigate(
          url,
          Page.NavigateOptions()
            .setTimeout(options.timeoutMs)
            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
        )
      }

      println("[PlaywrightSession] CDP connected, page ready")
      block(page, context)
    } finally {
      // 关闭本次创建的 tab，避免 Chrome 累积大量空 tab
      runCatching { page?.close() }
      // CDP 模式不关闭浏览器，只断开连接
      runCatching { browser.close() }
      runCatching { playwright.close() }
    }
  }

  /**
   * 等待 [context] 所有页面关闭（用于 non-headless 模式保持浏览器打开让用户手动操作）
   */
  fun waitUntilAllPagesClosed(context: BrowserContext) {
    runCatching {
      context.waitForCondition({ context.pages().isEmpty() })
    }
  }

  fun defaultProfileDir(): Path = Paths.get(
    System.getProperty("java.io.tmpdir"), "playwright-profile"
  )

  /** 删除 profile 目录下的 SingletonLock，避免上次进程异常退出后无法重启 */
  private fun cleanSingletonLock(profileDir: Path) {
    runCatching { Files.deleteIfExists(profileDir.resolve("SingletonLock")) }
    runCatching { Files.deleteIfExists(profileDir.resolve("SingletonSocket")) }
    runCatching { Files.deleteIfExists(profileDir.resolve("SingletonCookie")) }
  }

  /** 反自动化检测启动参数 */
  private val STEALTH_ARGS = listOf(
    "--disable-blink-features=AutomationControlled",
    "--disable-features=AutomationControlled,PasswordManager,PasswordManagerOnboarding,AutofillServerCommunication",
    "--disable-infobars",
    "--no-first-run",
    "--no-default-browser-check",
    "--disable-component-update",
    "--disable-background-networking",
    "--disable-save-password-bubble",
    "--password-store=basic",
  )

  /** 注入 JS 脚本隐藏 Playwright 自动化痕迹，帮助绕过 Cloudflare Turnstile */
  private fun injectStealthScripts(page: Page) {
    page.addInitScript("""
      // 隐藏 navigator.webdriver
      Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
      // 伪造 plugins（无头模式下为空数组会被检测）
      Object.defineProperty(navigator, 'plugins', {
        get: () => [1, 2, 3, 4, 5],
      });
      // 伪造 languages
      Object.defineProperty(navigator, 'languages', {
        get: () => ['en-US', 'en'],
      });
      // 隐藏 chrome.runtime 自动化标记
      window.chrome = { runtime: {} };
    """.trimIndent())
  }
}
