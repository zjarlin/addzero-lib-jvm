package site.addzero.cli.platform

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object ProUtil {

    // 处理进程输出（打印命令执行日志）
    @Throws(IOException::class)
    fun handleProcessOutput(process: Process) {
        BufferedReader(
            InputStreamReader(process.getInputStream())
        ).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                println("命令输出: " + line)
            }
        }
    }


    @Throws(IOException::class, InterruptedException::class)
    fun setUnixEnv(key: String, value: String?, configFile: String?) {
        // 1. 先删除已存在的同名变量配置
        val deleteCmd = "sed -i '/export " + key + "=/d' " + configFile
        val deleteProcess = ProcessBuilder("/bin/bash", "-c", deleteCmd).start()
        deleteProcess.waitFor()

        // 2. 写入新的环境变量配置
        val setCmd = "echo 'export " + key + "=\"" + value + "\"' >> " + configFile
        val setProcess = ProcessBuilder("/bin/bash", "-c", setCmd).start()

        handleProcessOutput(setProcess)
        val exitCode = setProcess.waitFor()
        if (exitCode != 0) {
            System.err.println("Unix设置环境变量失败，错误码: " + exitCode)
        }
        println("Unix环境变量设置成功（需执行 source " + configFile + " 生效）: " + key + "=" + value)
    }


}
