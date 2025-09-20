package site.addzero.cli.platform

import java.io.BufferedReader
import java.io.File
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


    // Unix类系统通用设置方法（写入配置文件）
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


    fun createUnixSymlink(target: File, link: File): Boolean {
        val command = "ln -s \"${target.absolutePath}\" \"${link.absolutePath}\""

        return try {
            val process = ProcessBuilder("/bin/sh", "-c", command)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()

            if (exitCode == 0) {
                true
            } else {
                System.err.println("Unix创建失败 (错误码: $exitCode): $output")
                false
            }
        } catch (e: Exception) {
            System.err.println("Unix创建异常: ${e.message}")
            false
        }

//        return false
    }

    fun createSymlink(absolutePath: String, linkPath: String): Boolean {
        val targetFile = File(absolutePath)
        val linkFile = File(linkPath)

        // 检查目标文件是否存在
        if (!targetFile.exists()) {
            System.err.println("目标文件不存在: $absolutePath")
            return false
        }

        // 检查链接是否已存在
        if (linkFile.exists()) {
            System.err.println("链接已存在: $linkPath")
            return false
        }

        return try {
            // 根据操作系统选择不同的实现
            when {
                PlatformService.isWindows() -> createWindowsSymlink(targetFile, linkFile)
                PlatformService.isLinux() || PlatformService.isMac() -> createUnixSymlink(targetFile, linkFile)
                else -> {
                    System.err.println("不支持的操作系统")
                    false
                }
            }
        } catch (e: Exception) {
            System.err.println("创建符号链接失败: ${e.message}")
            false
        }
    }

    private fun createWindowsSymlink(target: File, link: File): Boolean {
        val isDirectory = target.isDirectory
        val command = if (isDirectory) {
            "mklink /D \"${link.absolutePath}\" \"${target.absolutePath}\""
        } else {
            "mklink \"${link.absolutePath}\" \"${target.absolutePath}\""
        }

        return try {
            val process = ProcessBuilder("cmd.exe", "/c", command)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()

            if (exitCode == 0) {
                true
            } else {
                System.err.println("Windows创建失败 (错误码: $exitCode): $output")
                false
            }
        } catch (e: Exception) {
            System.err.println("Windows创建异常: ${e.message}")
            false
        }
    }

}
