package site.addzero.network.call.browser.windsurf

import com.microsoft.playwright.Page
import site.addzero.network.call.browser.core.BrowserAutomationOptions
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Windsurf 批量注册 + 绑卡 一站式工具
 *
 * 用法：
 * ```kotlin
 * // 串行：注册 3 个账号，注册完自动绑卡
 * WindsurfBatchRegistration.run(count = 3)
 *
 * // 并发：3 并发注册 10 个账号
 * WindsurfBatchRegistration.run(count = 10, concurrency = 3)
 *
 * // 仅注册不绑卡
 * WindsurfBatchRegistration.run(count = 5, bindCard = false)
 *
 * // 自定义密码和 CDP 端口
 * WindsurfBatchRegistration.run(
 *   count = 3,
 *   password = "MyPass123!",
 *   cdpUrl = "http://localhost:9222",
 * )
 * ```
 *
 * 并发说明：
 * - 每个线程使用独立的 [TempMailProvider] 实例
 * - 账号文件按邮箱名落盘，天然并发安全
 * - CDP 模式下多个线程共享同一个 Chrome，各开新 tab 互不干扰
 * - 串行模式（concurrency=1）最稳定，推荐先单流程跑通再开并发
 */
object WindsurfBatchRegistration {

  data class BatchResult(
    val total: Int,
    val success: Int,
    val failed: Int,
    val accounts: List<WindsurfAccount>,
    val errors: List<BatchError>,
  )

  data class BatchError(
    val index: Int,
    val email: String?,
    val error: String,
  )

  /**
   * 批量注册 Windsurf 账号
   *
   * @param count       需要注册的账号数量
   * @param concurrency 并发数（默认 1 = 串行）
   * @param password    Windsurf 密码，为 null 时使用默认 "StrongPass123!"
   * @param firstName   名（可选）
   * @param lastName    姓（可选）
   * @param bindCard    注册后是否自动绑卡（默认 true）
   * @param cardInfo    绑卡信息，为 null 时每个账号自动随机生成
   * @param cdpUrl      Chrome CDP 连接地址，为 null 时使用 Playwright 内置浏览器
   * @param storageDir  账号文件存储目录
   * @param timeoutMs   单个注册流程超时时间
   * @param slowMoMs    操作间延迟
   * @param mailProviderFactory 临时邮件提供者工厂，每个线程调用一次
   * @return [BatchResult] 批量注册结果
   */
  fun run(
    count: Int,
    concurrency: Int = 1,
    password: String? = "StrongPass123!",
    firstName: String? = "Auto",
    lastName: String? = "User",
    bindCard: Boolean = true,
    cardInfo: WindsurfCardInfo? = null,
    cdpUrl: String? = "http://localhost:9222",
    storageDir: Path = WindsurfAccountStorage.DEFAULT_DIR,
    timeoutMs: Double = 120_000.0,
    slowMoMs: Double = 500.0,
    mailProviderFactory: () -> TempMailProvider = { TempMailProvider.loadFromSpi() ?: error("No TempMailProvider via SPI") },
  ): BatchResult {
    require(count > 0) { "count must be > 0" }
    require(concurrency > 0) { "concurrency must be > 0" }

    val successAccounts = ConcurrentLinkedQueue<WindsurfAccount>()
    val errors = ConcurrentLinkedQueue<BatchError>()
    val completed = AtomicInteger(0)

    val options = WindsurfRegisterOptions(
      autoSubmit = true,
      automation = BrowserAutomationOptions(
        cdpUrl = cdpUrl,
        timeoutMs = timeoutMs,
        slowMoMs = slowMoMs,
      ),
    )

    val postAction: ((Page) -> Unit)? = if (bindCard) { page ->
      println("[Batch] starting card binding...")
      val card = cardInfo ?: WindsurfCardGenerator.generate()
      WindsurfCardBinding.bindCard(page, card)
    } else null

    println("═══════════════════════════════════════════════════")
    println("[Batch] starting batch registration: count=$count, concurrency=$concurrency, bindCard=$bindCard")
    println("═══════════════════════════════════════════════════")

    val startTime = System.currentTimeMillis()

    if (concurrency <= 1) {
      // 串行模式
      for (i in 1..count) {
        runSingle(
          index = i, total = count,
          password = password, firstName = firstName, lastName = lastName,
          options = options, storageDir = storageDir,
          postAction = postAction,
          mailProviderFactory = mailProviderFactory,
          successAccounts = successAccounts,
          errors = errors,
          completed = completed,
        )
      }
    } else {
      // 并发模式
      val executor = Executors.newFixedThreadPool(concurrency)
      for (i in 1..count) {
        executor.submit {
          runSingle(
            index = i, total = count,
            password = password, firstName = firstName, lastName = lastName,
            options = options, storageDir = storageDir,
            postAction = postAction,
            mailProviderFactory = mailProviderFactory,
            successAccounts = successAccounts,
            errors = errors,
            completed = completed,
          )
        }
      }
      executor.shutdown()
      executor.awaitTermination(count * timeoutMs.toLong() * 2, TimeUnit.MILLISECONDS)
    }

    val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
    val result = BatchResult(
      total = count,
      success = successAccounts.size,
      failed = errors.size,
      accounts = successAccounts.toList(),
      errors = errors.toList(),
    )

    println()
    println("═══════════════════════════════════════════════════")
    println("[Batch] DONE in ${elapsed}s")
    println("[Batch] success: ${result.success} / ${result.total}")
    println("[Batch] failed:  ${result.failed}")
    if (result.errors.isNotEmpty()) {
      println("[Batch] errors:")
      result.errors.forEach { e ->
        println("  #${e.index} ${e.email ?: "unknown"}: ${e.error}")
      }
    }
    println("[Batch] accounts saved to: $storageDir")
    println("═══════════════════════════════════════════════════")

    return result
  }

  private fun runSingle(
    index: Int,
    total: Int,
    password: String?,
    firstName: String?,
    lastName: String?,
    options: WindsurfRegisterOptions,
    storageDir: Path,
    postAction: ((Page) -> Unit)?,
    mailProviderFactory: () -> TempMailProvider,
    successAccounts: ConcurrentLinkedQueue<WindsurfAccount>,
    errors: ConcurrentLinkedQueue<BatchError>,
    completed: AtomicInteger,
  ) {
    val threadName = Thread.currentThread().name
    println()
    println("───────────────────────────────────────────────────")
    println("[Batch] [$threadName] starting #$index / $total")
    println("───────────────────────────────────────────────────")

    try {
      val account = WindsurfRegistration.registerWithTempMail(
        password = password,
        firstName = firstName,
        lastName = lastName,
        mailProvider = mailProviderFactory(),
        options = options,
        storageDir = storageDir,
        postRegistrationAction = postAction,
      )
      successAccounts.add(account)
      val done = completed.incrementAndGet()
      println("[Batch] [$threadName] #$index SUCCESS (${account.windsurfEmail}) — progress: $done/$total")
    } catch (ex: Throwable) {
      errors.add(BatchError(index = index, email = null, error = ex.message ?: ex.toString()))
      val done = completed.incrementAndGet()
      println("[Batch] [$threadName] #$index FAILED: ${ex.message} — progress: $done/$total")
    }
  }
}
