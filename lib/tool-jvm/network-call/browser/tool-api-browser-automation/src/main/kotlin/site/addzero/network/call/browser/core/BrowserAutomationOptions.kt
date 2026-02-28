package site.addzero.network.call.browser.core

/**
 * 浏览器自动化启动配置（通用，不绑定任何业务）
 */
data class BrowserAutomationOptions(
  /** 是否开启调试模式。true 时会弹出 Playwright Inspector 并在失败时保存截图和 HTML，同时自动关闭无头模式 */
  val debug: Boolean = false,
  /** 是否无头模式（不显示浏览器窗口）。debug=true 时此项强制为 false */
  val headless: Boolean = true,
  /** 页面操作的默认超时时间（毫秒）。查找元素、等待页面加载等均受此限制 */
  val timeoutMs: Double = 30_000.0,
  /** 每个操作之间的延迟（毫秒）。设为 500~1000 可以慢速观察填写过程，0 表示不延迟 */
  val slowMoMs: Double = 0.0,
  /** 调试产物（截图、HTML）的保存目录。为 null 时使用系统临时目录 */
  val artifactsDir: String? = null,
  /**
   * Chrome DevTools Protocol 连接地址。
   * 设置后通过 CDP 连接真实 Chrome（完全绕过 Turnstile 等反自动化检测）。
   *
   * 如果指定端口没有 Chrome 在监听，程序会**自动启动 Chrome** 并开启 CDP 端口（跨平台 macOS/Windows/Linux）。
   * 无需手动在终端执行任何命令。
   *
   * 示例：`cdpUrl = "http://localhost:9222"`
   */
  val cdpUrl: String? = null,
) {
  /** debug=true 时强制非无头，确保 Playwright Inspector 和浏览器窗口可见 */
  val effectiveHeadless: Boolean get() = if (debug) false else headless
}
