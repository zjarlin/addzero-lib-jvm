package site.addzero.network.call.browser.core

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * 自动启动 Chrome 并开启 CDP 远程调试端口（跨平台：macOS / Windows / Linux）
 *
 * 核心逻辑：
 * 1. 检测指定端口是否已有 Chrome CDP 在监听 → 有则直接复用
 * 2. 没有则自动检测系统 Chrome 路径并启动
 * 3. 等待 CDP 端口就绪后返回
 *
 * ```kotlin
 * // 自动启动 Chrome 并返回 CDP 地址
 * val cdpUrl = ChromeLauncher.ensureRunning(port = 9222)
 * // cdpUrl = "http://localhost:9222"
 * ```
 */
object ChromeLauncher {

  private val OS = System.getProperty("os.name", "").lowercase()
  private val IS_WINDOWS = "win" in OS
  private val IS_MAC = "mac" in OS || "darwin" in OS

  /** 正在运行的 Chrome 进程（由本类启动的），按端口号索引，支持多实例并发 */
  private val managedProcesses = java.util.concurrent.ConcurrentHashMap<Int, Process>()

  init {
    Runtime.getRuntime().addShutdownHook(Thread {
      managedProcesses.forEach { (port, proc) ->
        runCatching {
          println("[ChromeLauncher] shutting down Chrome on port $port")
          if (IS_WINDOWS) {
            Runtime.getRuntime().exec(arrayOf("taskkill", "/F", "/T", "/PID", proc.pid().toString()))
          } else {
            proc.destroy()
          }
          proc.waitFor(5, TimeUnit.SECONDS)
          if (proc.isAlive) proc.destroyForcibly()
        }
      }
      managedProcesses.clear()
    })
  }

  /**
   * 查找一个可用的空闲端口（用于并发场景下动态分配 CDP 端口）
   *
   * @param startPort 起始端口（默认 9222）
   * @param maxAttempts 最大尝试次数
   * @return 可用端口号
   */
  fun findFreePort(startPort: Int = 9222, maxAttempts: Int = 100): Int {
    for (offset in 0 until maxAttempts) {
      val port = startPort + offset
      if (!isPortInUse(port)) {
        return port
      }
    }
    error("No free port found in range $startPort..${startPort + maxAttempts - 1}")
  }

  /**
   * 检查端口是否被占用
   */
  private fun isPortInUse(port: Int): Boolean {
    return runCatching {
      java.net.ServerSocket(port).use { false }
    }.getOrDefault(true)
  }

  /**
   * 确保 Chrome CDP 在指定端口可用，返回 CDP URL。
   *
   * @param port           CDP 远程调试端口
   * @param userDataDir    Chrome profile 目录（隔离指纹用）。为 null 时自动生成
   * @param startTimeoutMs 启动超时时间（毫秒）
   * @return CDP URL，如 `http://localhost:9222`
   * @throws IllegalStateException Chrome 未找到或启动超时
   */
  fun ensureRunning(
    port: Int = 9222,
    userDataDir: String? = null,
    startTimeoutMs: Long = 30_000,
  ): String {
    val cdpUrl = "http://localhost:$port"

    // 1. 检查端口是否已有 CDP 在监听
    if (isCdpReady(cdpUrl)) {
      println("[ChromeLauncher] CDP already running at $cdpUrl")
      // 即使复用已有 Chrome，也尝试给 profile 目录写 Preferences（下次重启时生效）
      val existingProfileDir = userDataDir
        ?: Paths.get(System.getProperty("java.io.tmpdir"), "chrome-cdp-profile-$port").toString()
      runCatching { writeDisablePasswordPreferences(Paths.get(existingProfileDir)) }
      return cdpUrl
    }

    // 2. 找 Chrome 可执行文件
    val chromePath = ChromeResolver.resolve()
      ?: resolveFromPath()
      ?: error(buildNotFoundMessage())

    println("[ChromeLauncher] found Chrome: $chromePath")

    // 3. 准备 user-data-dir
    val profileDir = userDataDir
      ?: Paths.get(System.getProperty("java.io.tmpdir"), "chrome-cdp-profile-$port").toString()
    Files.createDirectories(Paths.get(profileDir))

    // 写入 Chrome Preferences 禁用密码管理器和翻译（命令行参数对已有 profile 不一定生效）
    writeDisablePasswordPreferences(Paths.get(profileDir))

    // 4. 启动 Chrome 进程
    val args = buildList {
      add(chromePath.toString())
      add("--remote-debugging-port=$port")
      add("--user-data-dir=$profileDir")
      add("--no-first-run")
      add("--no-default-browser-check")
      add("--disable-default-apps")
      add("--disable-popup-blocking")
      add("--disable-translate")
      add("--disable-background-timer-throttling")
      add("--disable-backgrounding-occluded-windows")
      add("--disable-renderer-backgrounding")
      // 禁用密码管理器弹框 + 崩溃恢复弹框
      add("--disable-save-password-bubble")
      add("--disable-component-update")
      add("--password-store=basic")
      add("--hide-crash-restore-bubble")
      add("--disable-session-crashed-bubble")
      add("--disable-features=PasswordManager,PasswordManagerOnboarding,AutofillServerCommunication,InfiniteSessionRestore,TranslateUI")
    }

    println("[ChromeLauncher] launching Chrome with CDP on port $port ...")
    val processBuilder = ProcessBuilder(args)
      .redirectErrorStream(true)

    val process = processBuilder.start()
    managedProcesses[port] = process

    // 消费 stdout/stderr 防止缓冲区满导致进程挂起
    Thread({
      BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        reader.lines().forEach { /* discard */ }
      }
    }, "chrome-stdout-drain").apply { isDaemon = true }.start()

    // 5. 等待 CDP 端口就绪
    val deadline = System.currentTimeMillis() + startTimeoutMs
    while (System.currentTimeMillis() < deadline) {
      if (!process.isAlive) {
        val exitCode = process.exitValue()
        // exit code 0 有时表示已有 Chrome 实例在运行（仅发了消息给已有实例）
        if (exitCode == 0 && isCdpReady(cdpUrl)) {
          println("[ChromeLauncher] Chrome delegated to existing instance, CDP ready at $cdpUrl")
          managedProcesses.remove(port)
          return cdpUrl
        }
        error("[ChromeLauncher] Chrome process exited immediately with code $exitCode. Is another Chrome instance running without --remote-debugging-port?")
      }
      if (isCdpReady(cdpUrl)) {
        println("[ChromeLauncher] CDP ready at $cdpUrl (profile: $profileDir)")
        return cdpUrl
      }
      Thread.sleep(500)
    }

    // 超时，清理
    process.destroyForcibly()
    managedProcesses.remove(port)
    error("[ChromeLauncher] Chrome CDP did not become ready within ${startTimeoutMs}ms on port $port")
  }

  /**
   * 检测 CDP 端口是否就绪（通过访问 /json/version 端点）
   */
  fun isCdpReady(cdpUrl: String): Boolean = try {
    val conn = URL("$cdpUrl/json/version").openConnection() as HttpURLConnection
    conn.connectTimeout = 1_000
    conn.readTimeout = 1_000
    conn.requestMethod = "GET"
    val code = conn.responseCode
    conn.disconnect()
    code == 200
  } catch (_: Exception) {
    false
  }

  /**
   * 从 PATH 环境变量中查找 Chrome
   */
  private fun resolveFromPath(): Path? {
    val names = if (IS_WINDOWS) {
      listOf("chrome.exe", "google-chrome.exe")
    } else {
      listOf("google-chrome", "google-chrome-stable", "chromium-browser", "chromium")
    }

    val pathDirs = System.getenv("PATH")?.split(if (IS_WINDOWS) ";" else ":") ?: return null
    for (dir in pathDirs) {
      for (name in names) {
        val candidate = Paths.get(dir, name)
        if (Files.isExecutable(candidate)) return candidate
      }
    }

    // macOS: 也尝试通过 `mdfind` 搜索
    if (IS_MAC) {
      return runCatching {
        val proc = ProcessBuilder("mdfind", "kMDItemCFBundleIdentifier == 'com.google.Chrome'")
          .redirectErrorStream(true)
          .start()
        val output = proc.inputStream.bufferedReader().readText().trim()
        proc.waitFor(3, TimeUnit.SECONDS)
        output.lineSequence()
          .map { Paths.get(it, "Contents", "MacOS", "Google Chrome") }
          .firstOrNull { Files.isExecutable(it) }
      }.getOrNull()
    }

    // Windows: 注册表查询
    if (IS_WINDOWS) {
      return runCatching {
        val proc = ProcessBuilder(
          "reg", "query",
          "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe",
          "/ve"
        ).redirectErrorStream(true).start()
        val output = proc.inputStream.bufferedReader().readText()
        proc.waitFor(3, TimeUnit.SECONDS)
        val match = Regex("REG_SZ\\s+(.+\\.exe)", RegexOption.IGNORE_CASE).find(output)
        match?.groupValues?.get(1)?.let { Paths.get(it.trim()) }?.takeIf { Files.exists(it) }
      }.getOrNull()
    }

    return null
  }

  /**
   * 写入 Chrome Preferences 禁用密码管理器、自动填充、翻译等弹框
   *
   * Chrome 的 Preferences 是 JSON 文件，位于 `{user-data-dir}/Default/Preferences`。
   * 命令行参数对已有 profile 不一定生效，直接改 Preferences 文件最可靠。
   */
  private fun writeDisablePasswordPreferences(userDataDir: Path) {
    val defaultDir = userDataDir.resolve("Default")
    Files.createDirectories(defaultDir)
    val prefsFile = defaultDir.resolve("Preferences")

    // 如果已有 Preferences 文件，读取并合并；否则创建新的
    val existingJson = if (Files.exists(prefsFile)) {
      runCatching { String(Files.readAllBytes(prefsFile)) }.getOrDefault("{}")
    } else "{}"

    // 简单 JSON 合并：在顶层插入/覆盖关键配置
    // 用正则替换不够可靠，但避免引入 JSON 库依赖
    val patchEntries = mapOf(
      "\"credentials_enable_service\"" to "false",
      "\"credentials_enable_autosignin\"" to "false",
    )

    var json = existingJson.trimEnd()
    // 确保是有效 JSON 对象
    if (!json.startsWith("{")) json = "{}"

    for ((key, value) in patchEntries) {
      if (key in json) {
        // 已有该 key，替换其值
        json = json.replace(Regex("$key\\s*:\\s*\\w+"), "$key: $value")
      } else {
        // 插入到开头 { 之后
        json = json.replaceFirst("{", "{ $key: $value,")
      }
    }

    // 禁用密码管理器的完整 Preferences 块
    if ("\"password_manager\"" !in json) {
      json = json.replaceFirst("{", """{ "password_manager": {"enabled": false, "leak_detection": false},""")
    }
    if ("\"translate\"" !in json) {
      json = json.replaceFirst("{", """{ "translate": {"enabled": false},""")
    }
    if ("\"translate_blocked_languages\"" !in json) {
      json = json.replaceFirst("{", """{ "translate_blocked_languages": ["zh-CN","zh-TW","en"],""")
    }
    // 防止"要恢复页面吗？"崩溃恢复弹框（Chrome 通过 exit_type 判断上次是否正常退出）
    if ("\"exit_type\"" in json) {
      json = json.replace(Regex("\"exit_type\"\\s*:\\s*\"[^\"]*\""), "\"exit_type\": \"Normal\"")
    } else {
      json = json.replaceFirst("{", """{ "exit_type": "Normal",""")
    }
    if ("\"exited_cleanly\"" in json) {
      json = json.replace(Regex("\"exited_cleanly\"\\s*:\\s*\\w+"), "\"exited_cleanly\": true")
    } else {
      json = json.replaceFirst("{", """{ "exited_cleanly": true,""")
    }

    Files.write(prefsFile, json.toByteArray())
    println("[ChromeLauncher] wrote Preferences to disable password manager & translate: $prefsFile")
  }

  private fun buildNotFoundMessage(): String = buildString {
    append("[ChromeLauncher] Chrome not found. ")
    when {
      IS_MAC -> append("Install Chrome or set it in PATH. Expected: /Applications/Google Chrome.app")
      IS_WINDOWS -> append("Install Chrome or add chrome.exe to PATH. Expected: C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")
      else -> append("Install google-chrome or chromium and ensure it's in PATH.")
    }
  }
}
