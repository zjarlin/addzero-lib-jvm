package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxCommandResult
import site.addzero.kbox.core.model.KboxCommandSpec
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Single
open class KboxCommandRunner {
    open fun isCommandAvailable(
        command: String,
    ): Boolean {
        val commandName = command.trim()
        if (commandName.isBlank()) {
            return false
        }
        val pathDirectories = System.getenv("PATH")
            .orEmpty()
            .split(File.pathSeparatorChar)
            .filter { it.isNotBlank() }
        val executableNames = buildExecutableNames(commandName)
        return pathDirectories.any { directory ->
            executableNames.any { executable ->
                File(directory, executable).canExecute()
            }
        }
    }

    open fun run(
        spec: KboxCommandSpec,
    ): KboxCommandResult {
        require(spec.command.isNotEmpty()) {
            "命令不能为空"
        }
        val startedAt = System.currentTimeMillis()
        val processBuilder = ProcessBuilder(spec.command)
        if (spec.workingDirectory.isNotBlank()) {
            processBuilder.directory(File(spec.workingDirectory))
        }
        processBuilder.environment().putAll(spec.environment)
        val process = processBuilder.start()
        var stdoutText = ""
        var stderrText = ""
        val stdoutThread = thread(start = true, isDaemon = true) {
            stdoutText = process.inputStream.bufferedReader().use { reader -> reader.readText() }
        }
        val stderrThread = thread(start = true, isDaemon = true) {
            stderrText = process.errorStream.bufferedReader().use { reader -> reader.readText() }
        }
        val finished = process.waitFor(spec.timeoutMillis, TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
        }
        stdoutThread.join()
        stderrThread.join()
        return KboxCommandResult(
            commandLine = spec.command.joinToString(separator = " ") { token ->
                if (token.any(Char::isWhitespace)) {
                    "\"$token\""
                } else {
                    token
                }
            },
            workingDirectory = spec.workingDirectory,
            exitCode = if (finished) {
                process.exitValue()
            } else {
                -1
            },
            stdout = stdoutText,
            stderr = stderrText,
            timedOut = !finished,
            durationMillis = System.currentTimeMillis() - startedAt,
        )
    }

    private fun buildExecutableNames(
        commandName: String,
    ): List<String> {
        val normalized = commandName.trim()
        val osName = System.getProperty("os.name").orEmpty()
        if (!osName.contains("Windows", ignoreCase = true)) {
            return listOf(normalized)
        }
        val extensions = System.getenv("PATHEXT")
            .orEmpty()
            .split(';')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .ifEmpty { listOf(".EXE", ".BAT", ".CMD") }
        if (normalized.contains('.')) {
            return listOf(normalized)
        }
        return buildList {
            add(normalized)
            extensions.forEach { extension ->
                add(normalized + extension.lowercase(Locale.ROOT))
                add(normalized + extension.uppercase(Locale.ROOT))
            }
        }
    }
}
