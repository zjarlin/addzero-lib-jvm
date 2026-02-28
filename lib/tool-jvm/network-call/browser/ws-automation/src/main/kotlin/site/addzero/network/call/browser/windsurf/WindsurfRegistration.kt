package site.addzero.network.call.browser.windsurf

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState
import site.addzero.network.call.browser.core.ChromeLauncher
import site.addzero.network.call.browser.core.PlaywrightSession
import java.nio.file.Paths

/**
 * Windsurf 注册一键完成门面
 *
 * 编排三步注册流程，支持两种验证码获取方式：
 * 1. **手动模式**：`verificationCode` 为 null → 浏览器保持打开，手动输入验证码
 * 2. **全自动模式**：注入 [TempMailProvider] → 自动获取临时邮箱、接收验证码、完成注册
 *
 * ```kotlin
 * // 手动模式
 * WindsurfRegistration.register(
 *   form = WindsurfRegisterForm(email = "xxx@yyy.com", password = "abc123"),
 * )
 *
 * // 全自动模式（SPI 或手动注入 TempMailProvider）
 * WindsurfRegistration.registerWithTempMail(
 *   password = "abc123",
 *   mailProvider = MyTempMailProvider(),
 * )
 * ```
 */
object WindsurfRegistration {

  private const val REGISTER_URL = "https://windsurf.com/account/register"

  /** 邮箱 API 调用的全局锁（串行化 TLS 连接，避免并发触发 rate-limit） */
  private val mailApiLock = java.util.concurrent.locks.ReentrantLock()

  /** 账号文件默认保存目录，可通过 [WindsurfAccountStorage.DEFAULT_DIR] 查看实际路径 */
  val accountStorageDir get() = WindsurfAccountStorage.DEFAULT_DIR

  /**
   * 手动/半自动注册
   *
   * 如果 [WindsurfRegisterForm.verificationCode] 为 null，填完密码后浏览器保持打开，等待手动输入验证码。
   */
  fun register(
    form: WindsurfRegisterForm,
    options: WindsurfRegisterOptions = WindsurfRegisterOptions(),
  ) {
    val profileDir = Paths.get(options.userDataDir ?: PlaywrightSession.defaultProfileDir().toString())

    PlaywrightSession.withPersistentPage(
      url = REGISTER_URL,
      profileDir = profileDir,
      options = options.automation,
    ) { page, context ->

      val step1Result = WindsurfSteps.step1_fillBasicInfoAndContinue(
        page = page,
        email = form.email,
        firstName = form.firstName,
        lastName = form.lastName,
      )

      if (step1Result == WindsurfSteps.Step1Result.ALREADY_REGISTERED) {
        println("[WindsurfRegistration] already registered, skipping step2/step3")
      } else {
        WindsurfSteps.step2_fillPasswordAndContinue(
          page = page,
          password = form.password,
          confirmPassword = form.confirmPassword ?: form.password,
        )

        if (form.verificationCode != null) {
          WindsurfSteps.step3_fillVerificationCodeAndSubmit(page, form.verificationCode)
          println("[WindsurfRegistration] registration completed")
        } else {
          println("[WindsurfRegistration] waiting for manual verification code input...")
        }
      }

      if (!options.automation.effectiveHeadless) {
        println("[WindsurfRegistration] browser stays open, close window to exit")
        PlaywrightSession.waitUntilAllPagesClosed(context)
      }
    }
  }

  /**
   * 全自动注册（通过 [TempMailProvider] 自动获取邮箱和验证码）
   *
   * 说明：临时邮箱创建后会立即落盘，即便注册失败也能保留邮箱账号。
   *
   * @param password      账号密码
   * @param firstName     可选名
   * @param lastName      可选姓
   * @param mailProvider  临时邮件提供者，为 null 时通过 SPI 自动加载
   * @param options       浏览器和业务选项
   * @param saveAccount   是否保存状态更新（默认 true）。临时邮箱创建总是会落盘。
   * @param storageDir    账号文件保存目录，默认 [WindsurfAccountStorage.DEFAULT_DIR]
   * @return 注册结果的 [WindsurfAccount]（包含邮箱、Windsurf 密码、临时邮箱密码）
   */
  fun registerWithTempMail(
    password: String? = null,
    firstName: String? = null,
    lastName: String? = null,
    mailProvider: TempMailProvider? = null,
    options: WindsurfRegisterOptions = WindsurfRegisterOptions(),
    saveAccount: Boolean = true,
    storageDir: java.nio.file.Path = WindsurfAccountStorage.DEFAULT_DIR,
    postRegistrationAction: ((Page) -> Unit)? = null,
  ): WindsurfAccount {
    val provider = mailProvider
      ?: TempMailProvider.loadFromSpi()
      ?: error("No TempMailProvider found. Provide one or register via SPI in META-INF/services/")

    // 优先消费存储目录中未成功注册的账号（claimPending 是 synchronized 的，并发安全）
    val pending = WindsurfAccountStorage.claimPending(storageDir)
    val reusing = pending != null

    val email: String
    val windsurfPassword: String
    var account: WindsurfAccount

    // 串行化邮箱 API 调用（TLS 握手），避免并发触发 rate-limit
    mailApiLock.lock()
    try {
      if (pending != null) {
        email = pending.windsurfEmail
        windsurfPassword = password ?: pending.windsurfPassword
        account = pending.copy(
          windsurfPassword = windsurfPassword,
          status = WindsurfAccountStatus.IN_PROGRESS,
          errorMessage = null,
        )
        println("[WindsurfRegistration] reusing pending account: $email (was ${pending.status})")
        // 复用邮箱时需要让 provider 登录已有邮箱以便后续收验证码
        provider.loginExisting(email, pending.mailPassword)
      } else {
        email = provider.createEmail()
        windsurfPassword = password ?: provider.getMailPassword()
        account = WindsurfAccount(
          windsurfEmail = email,
          windsurfPassword = windsurfPassword,
          mailPassword = provider.getMailPassword(),
          status = WindsurfAccountStatus.EMAIL_CREATED,
        )
        println("[WindsurfRegistration] created new temp email: $email")
        // 新邮箱立即落盘
        val createdPath = WindsurfAccountStorage.save(account, storageDir)
        println("[WindsurfRegistration] temp mail saved: $createdPath")
      }
    } finally {
      mailApiLock.unlock()
    }

    // 如果指定了 cdpPort，使用独立 Chrome 实例；否则使用默认 9222（共享模式）
    val cdpPort = options.cdpPort ?: 9222
    
    // 每个 CDP 端口使用独立 profile 目录（Chrome 不允许多进程共享同一 user-data-dir）
    val baseProfileDir = options.userDataDir ?: PlaywrightSession.defaultProfileDir().toString() + "-windsurf"
    val profileDir = if (cdpPort == 9222) {
      // 默认端口保持原路径（兼容旧行为）
      Paths.get(baseProfileDir)
    } else {
      // 并发模式：每个端口独立目录
      Paths.get("$baseProfileDir-$cdpPort")
    }
    println("[WindsurfRegistration] using profile: $profileDir (CDP port: $cdpPort)")

    val cdpUrl = ChromeLauncher.ensureRunning(port = cdpPort, userDataDir = profileDir.toString())
    val automationOptions = options.automation.copy(cdpUrl = cdpUrl)

    try {
      PlaywrightSession.withPersistentPage(
        url = null,
        profileDir = profileDir,
        options = automationOptions,
      ) { page, context ->

        // 清除 Windsurf 登录态 cookie，保留 Cloudflare Turnstile cookie
        clearWindsurfAuthCookies(context)
        println("[WindsurfRegistration] cleared Windsurf auth cookies, navigating to register page...")

        // 导航到注册页（带重试，CDP 模式下偶发 ERR_SSL_PROTOCOL_ERROR）
        navigateWithRetry(page, REGISTER_URL, options.automation.timeoutMs)

        // 关闭 cookie consent banner（"We use cookies" 弹框）
        dismissCookieBanner(page)

        val urlAfterNav = page.url()
        val alreadyLoggedIn = urlAfterNav.contains("/profile") || urlAfterNav.contains("/dashboard")

        if (alreadyLoggedIn) {
          // 注册页被重定向到 profile → 检查是否是当前邮箱
          val isCurrentEmail = runCatching {
            page.locator("text=$email").first().isVisible
          }.getOrDefault(false)
          if (isCurrentEmail) {
            println("[WindsurfRegistration] already registered as $email, skipping")
          } else {
            // 残留其他账号 session，清除全部 cookie 后重试
            println("[WindsurfRegistration] residual session from another account, clearing all cookies...")
            context.clearCookies()
            navigateWithRetry(page, REGISTER_URL, options.automation.timeoutMs)
            dismissCookieBanner(page)
            // 清完 cookie 后走正常注册流程（下面统一处理）
            doRegistrationFlow(page, email, windsurfPassword, firstName, lastName, provider)
          }
        } else {
          doRegistrationFlow(page, email, windsurfPassword, firstName, lastName, provider)
        }

        if (postRegistrationAction != null) {
          println("[WindsurfRegistration] executing post-registration action (e.g. card binding)...")
          postRegistrationAction(page)
        }

        if (!options.automation.effectiveHeadless) {
          println("[WindsurfRegistration] browser stays open, close window to exit")
          PlaywrightSession.waitUntilAllPagesClosed(context)
        }
      }

      account = account.copy(status = WindsurfAccountStatus.REGISTERED, errorMessage = null)
      if (saveAccount) {
        val savedPath = WindsurfAccountStorage.save(account, storageDir)
        println("[WindsurfRegistration] account saved: $savedPath")
      }
      return account
    } catch (ex: Throwable) {
      account = account.copy(status = WindsurfAccountStatus.FAILED, errorMessage = ex.message)
      if (saveAccount) {
        val savedPath = WindsurfAccountStorage.save(account, storageDir)
        println("[WindsurfRegistration] account saved (failed): $savedPath")
      }
      throw ex
    }
  }

  /**
   * 完整注册流程：step1 → step2 → step3
   *
   * 根据 step1 返回值决定是否继续：
   * - [WindsurfSteps.Step1Result.PASSWORD_PAGE] → 继续 step2、step3
   * - [WindsurfSteps.Step1Result.ALREADY_REGISTERED] → 跳过（邮箱已注册）
   */
  private fun doRegistrationFlow(
    page: Page,
    email: String,
    password: String,
    firstName: String?,
    lastName: String?,
    provider: TempMailProvider,
  ) {
    val step1Result = WindsurfSteps.step1_fillBasicInfoAndContinue(
      page = page,
      email = email,
      firstName = firstName,
      lastName = lastName,
    )

    if (step1Result == WindsurfSteps.Step1Result.ALREADY_REGISTERED) {
      println("[WindsurfRegistration] step1 reports already registered for $email, skipping step2/step3")
      return
    }

    WindsurfSteps.step2_fillPasswordAndContinue(
      page = page,
      password = password,
    )

    // 检测 step2 后页面状态
    val urlAfterStep2 = page.url()
    println("[WindsurfRegistration] after step2, URL=$urlAfterStep2")

    if (urlAfterStep2.contains("/profile") || urlAfterStep2.contains("/dashboard")) {
      println("[WindsurfRegistration] redirected to profile after step2 (already registered)")
      return
    }

    // 检查是否卡在注册页且有错误提示
    if (urlAfterStep2.contains("/register") && !urlAfterStep2.contains("verify")) {
      val errorText = runCatching {
        val errEl = page.locator("form [role='alert'], form .error-message, form .text-red-500, form .text-destructive").first()
        if (errEl.isVisible) errEl.textContent()?.trim() else null
      }.getOrNull()
      if (!errorText.isNullOrBlank()) {
        error("[WindsurfRegistration] registration blocked: $errorText (email=$email)")
      }
      println("[WindsurfRegistration] still on register page but no error detected, continuing...")
    }

    // 获取验证码并提交
    println("[WindsurfRegistration] waiting for verification code from $email ...")
    val code = provider.fetchVerificationCode(email)
    println("[WindsurfRegistration] got verification code: $code")
    WindsurfSteps.step3_fillVerificationCodeAndSubmit(page, code)
    println("[WindsurfRegistration] registration completed for $email")
  }

  /**
   * 带重试的页面导航（CDP 模式下偶发 ERR_SSL_PROTOCOL_ERROR）
   */
  private fun navigateWithRetry(page: Page, url: String, timeoutMs: Double, maxRetries: Int = 3) {
    var lastError: Throwable? = null
    for (attempt in 1..maxRetries) {
      try {
        page.navigate(
          url,
          Page.NavigateOptions()
            .setTimeout(timeoutMs)
            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
        )
        return
      } catch (e: Throwable) {
        lastError = e
        if (attempt < maxRetries) {
          println("[WindsurfRegistration] navigate failed (attempt $attempt/$maxRetries): ${e.message?.take(80)}, retrying in 3s...")
          Thread.sleep(3_000)
        }
      }
    }
    throw lastError!!
  }

  /**
   * 关闭 cookie consent banner（"We use cookies" 弹框）
   *
   * 点击 "Reject all" 拒绝所有 cookie（避免干扰自动化），
   * 找不到则尝试 "Accept all"，都没有就跳过。
   */
  private fun dismissCookieBanner(page: Page) {
    runCatching {
      // 等最多 3 秒看 banner 是否出现
      val rejectBtn = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
        Page.GetByRoleOptions().setName("Reject all"))
      if (rejectBtn.count() > 0 && rejectBtn.first().isVisible) {
        rejectBtn.first().click(com.microsoft.playwright.Locator.ClickOptions().setTimeout(3000.0))
        println("[WindsurfRegistration] dismissed cookie banner (Reject all)")
        return
      }
      val acceptBtn = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
        Page.GetByRoleOptions().setName("Accept all"))
      if (acceptBtn.count() > 0 && acceptBtn.first().isVisible) {
        acceptBtn.first().click(com.microsoft.playwright.Locator.ClickOptions().setTimeout(3000.0))
        println("[WindsurfRegistration] dismissed cookie banner (Accept all)")
        return
      }
    }
  }

  private fun clearWindsurfAuthCookies(context: BrowserContext) {
    val cfCookieNames = setOf("cf_clearance", "__cf_bm", "cf_chl_2", "cf_chl_prog")
    val cookies = context.cookies()
    val toRemove = cookies.filter { cookie ->
      cookie.domain.contains("windsurf") && cookie.name !in cfCookieNames
    }
    if (toRemove.isNotEmpty()) {
      // Playwright 没有删除单个 cookie 的 API，只能全部清除后重新添加要保留的
      val toKeep = cookies.filter { cookie ->
        !cookie.domain.contains("windsurf") || cookie.name in cfCookieNames
      }
      context.clearCookies()
      if (toKeep.isNotEmpty()) {
        context.addCookies(toKeep.map { cookie ->
          com.microsoft.playwright.options.Cookie(cookie.name, cookie.value)
            .setDomain(cookie.domain)
            .setPath(cookie.path)
            .setExpires(cookie.expires)
            .setHttpOnly(cookie.httpOnly)
            .setSecure(cookie.secure)
            .setSameSite(cookie.sameSite)
        })
      }
      println("[WindsurfRegistration] removed ${toRemove.size} windsurf cookies, kept ${toKeep.size}")
    }
  }
}
