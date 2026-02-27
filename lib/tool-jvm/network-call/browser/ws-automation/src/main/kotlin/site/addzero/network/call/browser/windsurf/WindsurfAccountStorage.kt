package site.addzero.network.call.browser.windsurf

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

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
@Suppress("unused")
object WindsurfAccountStorage {

  /** 默认账号存储目录 */
  val DEFAULT_DIR: Path = Paths.get(System.getProperty("user.home"), "windsurf-accounts")

  private val mapper = jacksonObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  /**
   * 将账号信息保存到磁盘
   *
   * 文件名由邮箱地址唯一决定，并发调用同一邮箱是幂等的。
   *
   * @param account  要保存的账号
   * @param dir      存储目录，默认 [DEFAULT_DIR]
   * @return         实际写入的文件路径
   */
  fun save(account: WindsurfAccount, dir: Path = DEFAULT_DIR): Path {
    Files.createDirectories(dir)

    val fileName = emailToFileName(account.windsurfEmail)
    val target = dir.resolve(fileName)
    val tmp = dir.resolve(".$fileName.tmp")

    // 原子写：写临时文件 → rename，避免并发读到半写状态
    Files.newBufferedWriter(tmp).use { writer ->
      mapper.writeValue(writer, account)
    }
    runCatching {
      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }.onFailure {
      // ATOMIC_MOVE 在跨设备时可能不支持，fallback 到普通 replace
      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
    }

    println("[WindsurfAccountStorage] saved: $target")
    return target
  }

  /**
   * 从目录加载所有账号
   *
   * @param dir 存储目录，默认 [DEFAULT_DIR]
   */
  fun loadAll(dir: Path = DEFAULT_DIR): List<WindsurfAccount> {
    if (!Files.exists(dir)) return emptyList()
    val paths = Files.list(dir).use { stream ->
      stream.filter { path ->
        val name = path.fileName.toString()
        name.endsWith(".json") && !name.startsWith(".")
      }.toList()
    }
    return paths.mapNotNull { path ->
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
   * 查找未成功注册的账号（状态为 [WindsurfAccountStatus.EMAIL_CREATED] 或 [WindsurfAccountStatus.FAILED]）
   *
   * 按写入时间升序排列，优先消费最早创建的。
   */
  fun findPending(dir: Path = DEFAULT_DIR): List<WindsurfAccount> =
    loadAll(dir).filter { it.status != WindsurfAccountStatus.REGISTERED }
      .sortedBy { it.registeredAt }

  private fun emailToFileName(email: String): String =
    email.replace("@", "_at_").replace(Regex("[^a-zA-Z0-9._\\-]"), "_") + ".json"
}
