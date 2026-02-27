package site.addzero.network.call.browser.core

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 检测系统中可用的 Chrome / Chromium 可执行文件路径
 */
internal object ChromeResolver {

  private val CANDIDATES = listOf(
    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
    "/usr/bin/google-chrome",
    "/usr/bin/google-chrome-stable",
    "/usr/bin/chromium-browser",
    "/usr/bin/chromium",
    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
  )

  fun resolve(): Path? = CANDIDATES
    .map { Paths.get(it) }
    .firstOrNull { Files.exists(it) }
}
