package site.addzero.network.call.browser.core

/**
 * 浏览器自动化启动配置（通用，不绑定任何业务）
 */
data class BrowserAutomationOptions(
  val headless: Boolean = true,
  val timeoutMs: Double = 30_000.0,
  val slowMoMs: Double = 0.0,
  val debug: Boolean = false,
  val artifactsDir: String? = null,
)
