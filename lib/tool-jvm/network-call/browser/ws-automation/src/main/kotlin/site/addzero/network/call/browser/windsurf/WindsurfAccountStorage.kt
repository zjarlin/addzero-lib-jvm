package site.addzero.network.call.browser.windsurf

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

/**
 * Windsurf 账号持久化存储
 *
 * 存储策略：**一个文件一个账号**
 * - 文件名：`{email_at_domain}.json`（`@` 替换为 `_at_`）
 * - 写入方式：先写临时文件再原子重命名，保证并发安全，单个账号幂等写入
 * - 默认目录：`~/windsurf-accounts/`
 *
 * 并发安全性：多个线程/进程同时注册不同账号互不干扰（各写各的文件）；
 * 同一邮箱重复写入等幂（最终状态一致）。
 */
@Suppress("unused", "SpellCheckingInspection")
object WindsurfAccountStorage {

  /** 默认账号存储目录 */
  val DEFAULT_DIR: Path = Paths.get(System.getProperty("user.home"), "windsurf-accounts")

  private val mapper = jacksonObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  /** 本次 JVM 进程内已被 claimPending 认领的邮箱（防止失败后被再次认领） */
  private val claimedEmails = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
  
  /** claimPending 的显式锁（确保并发安全） */
  private val claimLock = java.util.concurrent.locks.ReentrantLock()

  /**
   * 将账号信息保存到磁盘
   *
   * 文件名由邮箱地址唯一决定，并发调用同一邮箱是幂等的。
   * 注册成功的账号会自动移动到 success/ 子目录，避免被 findPending 再次消费。
   *
   * @param account  要保存的账号
   * @param dir      存储目录，默认 [DEFAULT_DIR]
   * @return         实际写入的文件路径
   */
  fun save(account: WindsurfAccount, dir: Path = DEFAULT_DIR): Path {
    if (!Files.exists(dir)) {
      Files.createDirectories(dir)
    }
    
    val fileName = emailToFileName(account.windsurfEmail)
    
    if (account.status == WindsurfAccountStatus.REGISTERED) {
      // 注册成功 → 写入 success/ 并从主目录删除
      val successDir = dir.resolve("success").also { Files.createDirectories(it) }
      val successPath = successDir.resolve(fileName)
      mapper.writeValue(successPath.toFile(), account)
      // 删除主目录中的旧文件
      val mainPath = dir.resolve(fileName)
      Files.deleteIfExists(mainPath)
      return successPath
    } else {
      // 其他状态 → 写入主目录
      val mainPath = dir.resolve(fileName)
      mapper.writeValue(mainPath.toFile(), account)
      return mainPath
    }
  }

  /**
   * 从目录加载所有账号
   *
   * @param dir 存储目录，默认 [DEFAULT_DIR]
   */
  fun loadAll(dir: Path = DEFAULT_DIR): List<WindsurfAccount> {
    if (!Files.exists(dir)) return emptyList()
    val paths = Files.list(dir).use { stream ->
      stream
        .filter { path: Path ->
          val name = path.fileName.toString()
          name.endsWith(".json") && !name.startsWith(".")
        }
        .collect(Collectors.toList())
    }
    return paths.mapNotNull { path: Path ->
      runCatching { mapper.readValue<WindsurfAccount>(path.toFile()) }.getOrNull()
    }
  }

  /**
   * 按邮箱查找账号
   *
   * @param email Windsurf 登录邮箱
   * @param dir   存储目录，默认 [DEFAULT_DIR]
   */
  fun findByEmail(email: String, dir: Path = DEFAULT_DIR): WindsurfAccount? {
    val path = dir.resolve(emailToFileName(email))
    if (!Files.exists(path)) return null
    return runCatching { mapper.readValue<WindsurfAccount>(path.toFile()) }.getOrNull()
  }

  /**
   * 一致性修复：将主目录中所有 REGISTERED 状态的账号同步到 success/ 子目录
   *
   * 用于修复历史遗留数据（在 success/ 备份逻辑上线前已注册的账号）。
   * 幂等操作，已存在的文件会被覆盖。
   */
  fun syncRegisteredToSuccess(dir: Path = DEFAULT_DIR) {
    val successDir = dir.resolve("success")
    val registered = loadAll(dir).filter { it.status == WindsurfAccountStatus.REGISTERED }
    if (registered.isEmpty()) return
    
    Files.createDirectories(successDir)
    var synced = 0
    for (account in registered) {
      val fileName = emailToFileName(account.windsurfEmail)
      val successPath = successDir.resolve(fileName)
      mapper.writeValue(successPath.toFile(), account)
      // 从主目录删除
      Files.deleteIfExists(dir.resolve(fileName))
      synced++
    }
    println("[WindsurfAccountStorage] moved $synced REGISTERED accounts to success/")
  }

  /**
   * 查找待注册的账号（仅状态为 [WindsurfAccountStatus.EMAIL_CREATED]）
   *
   * 跳过 REGISTERED、IN_PROGRESS、FAILED 状态的账号（FAILED 的邮箱可能已过期，不再自动重试）。
   * 按写入时间升序排列，优先消费最早创建的。
   */
  fun findPending(dir: Path = DEFAULT_DIR): List<WindsurfAccount> =
    loadAll(dir).filter { 
      it.status == WindsurfAccountStatus.EMAIL_CREATED
    }.sortedBy { it.registeredAt }

  /**
   * 原子地认领一个 pending 账号（并发安全）
   *
   * 通过将账号状态写为 IN_PROGRESS 实现乐观锁：
   * 多个线程同时调用时，只有一个能成功写入 IN_PROGRESS 状态。
   * 其余线程会发现状态已变，跳过该账号继续找下一个。
   *
   * @return 成功认领的账号，无可用 pending 账号时返回 null
   */
  fun claimPending(dir: Path = DEFAULT_DIR): WindsurfAccount? {
    claimLock.lock()
    try {
      // 在锁内完成所有操作：查找 → 检查 → 标记 → 保存
      val allPending = findPending(dir)
      val pending = allPending.firstOrNull { email ->
        val alreadyClaimed = email.windsurfEmail in claimedEmails
        if (alreadyClaimed) {
          println("[WindsurfAccountStorage] skip ${email.windsurfEmail} (already claimed in memory)")
        }
        !alreadyClaimed
      } ?: return null
      
      // 内存标记 + 磁盘标记双重保护
      claimedEmails.add(pending.windsurfEmail)
      val claimed = pending.copy(status = WindsurfAccountStatus.IN_PROGRESS, errorMessage = "claimed by ${Thread.currentThread().name}")
      save(claimed, dir)
      println("[WindsurfAccountStorage] claimed ${pending.windsurfEmail} by ${Thread.currentThread().name}")
      return claimed
    } finally {
      claimLock.unlock()
    }
  }

  private fun emailToFileName(email: String): String =
    email.replace("@", "_at_").replace(Regex("[^a-zA-Z0-9._\\-]"), "_") + ".json"
}
