package site.addzero.network.call.suno.log

import org.koin.core.annotation.Singleton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Singleton
class FileSunoLogStrategy : SunoLogStrategy {
  private val cacheDir: File by lazy {
    val userHome = System.getProperty("user.home")
    val dir = File(userHome, ".cache/suno-vectorengine")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())

  override fun log(bizName: String, requestBodyString: String, responseString: String) {
    try {
      val timestamp = dateFormat.format(Date())
      val logFile = File(cacheDir, "${bizName}_$timestamp.json")

      val content = """
                {
                    "bizName": "$bizName",
                    "timestamp": "$timestamp",
                    "request": $requestBodyString,
                    "response": $responseString
                }
            """.trimIndent()

      logFile.writeText(content)
    } catch (e: Exception) {
      // 静默失败，不影响主流程
      e.printStackTrace()
    }
  }
}
