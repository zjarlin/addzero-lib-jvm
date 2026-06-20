package site.addzero.common.kt_util

object CmdUtil {

    /**
     * 执行当前系统适配的终端命令并返回输出内容
     */
    fun runCmd(cmd: String): String {
        val processBuilder = ProcessBuilder(commandParts(cmd))
        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().use { reader ->
            reader.readText()
        }
        process.waitFor()

        return output
    }

    fun quoteArg(value: String): String {
        return if (isWindows()) {
            "'${value.replace("'", "''")}'"
        } else {
            "'${value.replace("'", "'\"'\"'")}'"
        }
    }

    private fun commandParts(cmd: String): List<String> {
        return if (isWindows()) {
            listOf("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", cmd)
        } else {
            listOf("/bin/bash", "-c", cmd)
        }
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
    }
}
