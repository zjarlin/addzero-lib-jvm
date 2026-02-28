package site.addzero.network.call.browser.core

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * 检测系统中可用的 Chrome / Chromium 可执行文件路径
 */
object ChromeResolver {

  private val CANDIDATES = buildList {
    // macOS
    add("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    add("/Applications/Chromium.app/Contents/MacOS/Chromium")
    // Linux
    add("/usr/bin/google-chrome")
    add("/usr/bin/google-chrome-stable")
    add("/usr/bin/chromium-browser")
    add("/usr/bin/chromium")
    add("/snap/bin/chromium")
    // Windows — system-wide
    add("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe")
    add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe")
    // Windows — per-user install
    val localAppData = System.getenv("LOCALAPPDATA")
    if (localAppData != null) {
      add("$localAppData\\Google\\Chrome\\Application\\chrome.exe")
    }
    // Windows — Edge as Chromium fallback
    add("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe")
    add("C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe")
  }

  fun resolve(): Path? = CANDIDATES
    .map { Paths.get(it) }
    .firstOrNull { Files.exists(it) }
}
