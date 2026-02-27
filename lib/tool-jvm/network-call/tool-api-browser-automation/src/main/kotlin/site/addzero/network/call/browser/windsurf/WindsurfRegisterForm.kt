package site.addzero.network.call.browser.windsurf

import site.addzero.network.call.browser.core.BrowserAutomationOptions

/**
 * Windsurf 注册表单数据（两步流程）
 *
 * 第一步：firstName + lastName + email + 勾选协议 + Continue
 * 第二步：password + confirmPassword + Continue
 */
data class WindsurfRegisterForm(
  val email: String,
  val password: String,
  val firstName: String? = null,
  val lastName: String? = null,
  val confirmPassword: String? = null,
)

/**
 * Windsurf 注册业务选项
 */
data class WindsurfRegisterOptions(
  val autoSubmit: Boolean = false,
  val automation: BrowserAutomationOptions = BrowserAutomationOptions(),
  /** 持久化浏览器 profile 目录，用于绕过 Cloudflare Turnstile。为 null 时使用临时目录 */
  val userDataDir: String? = null,
)
