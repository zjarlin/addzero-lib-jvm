package site.addzero.kbox.core.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxDetectedPackageManager
import site.addzero.kbox.core.model.KboxPackageImportEntryResult
import site.addzero.kbox.core.model.KboxCommandSpec
import kotlin.io.path.createTempFile

abstract class AbstractPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : KboxPackageManagerAdapter {
    protected val currentOsName: String
        get() = System.getProperty("os.name").orEmpty()

    final override fun detect(): KboxDetectedPackageManager {
        if (!supportsCurrentOs()) {
            return KboxDetectedPackageManager(
                managerId = managerId,
                displayName = displayName,
                available = false,
                detail = "当前系统不支持",
            )
        }
        return if (isAvailable()) {
            KboxDetectedPackageManager(
                managerId = managerId,
                displayName = displayName,
                available = true,
                detail = "可用",
            )
        } else {
            KboxDetectedPackageManager(
                managerId = managerId,
                displayName = displayName,
                available = false,
                detail = "命令不可用",
            )
        }
    }

    final override fun exportInstalledPackages(): List<String> {
        require(supportsCurrentOs()) {
            "$displayName 当前系统不支持"
        }
        require(isAvailable()) {
            "$displayName 命令不可用"
        }
        return doExportInstalledPackages()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    protected abstract fun supportsCurrentOs(): Boolean

    protected abstract fun isAvailable(): Boolean

    protected abstract fun doExportInstalledPackages(): List<String>

    protected fun runCommand(
        command: List<String>,
        workingDirectory: String = "",
    ) = runner.run(
        KboxCommandSpec(
            command = command,
            workingDirectory = workingDirectory,
        ),
    )

    protected fun commandExists(
        command: String,
    ): Boolean {
        return runner.isCommandAvailable(command)
    }

    protected fun elevatedCommand(
        command: List<String>,
    ): List<String> {
        return if (commandExists("sudo")) {
            listOf("sudo") + command
        } else {
            command
        }
    }

    protected fun packageImportResult(
        attemptedPackages: List<String>,
        output: String,
        success: Boolean,
    ): KboxPackageImportEntryResult {
        return if (success) {
            KboxPackageImportEntryResult(
                managerId = managerId,
                displayName = displayName,
                attemptedPackages = attemptedPackages,
                installedPackages = attemptedPackages,
                output = output,
            )
        } else {
            KboxPackageImportEntryResult(
                managerId = managerId,
                displayName = displayName,
                attemptedPackages = attemptedPackages,
                failedPackages = attemptedPackages,
                output = output,
            )
        }
    }

    protected fun parseLines(
        rawText: String,
    ): List<String> {
        return rawText.lineSequence()
            .map { line -> line.trim() }
            .filter { line -> line.isNotEmpty() }
            .toList()
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class HomebrewFormulaPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "homebrew-formula"
    override val displayName: String = "Homebrew Formula"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Mac", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("brew")
    }

    override fun doExportInstalledPackages(): List<String> {
        val result = runCommand(listOf("brew", "list", "--formula"))
        check(result.success) { result.output.ifBlank { "读取 Homebrew Formula 失败" } }
        return parseLines(result.stdout)
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(
                managerId = managerId,
                displayName = displayName,
                skippedPackages = emptyList(),
                output = "无缺失包",
            )
        }
        val result = runCommand(listOf("brew", "install") + packages)
        return packageImportResult(packages, result.output, result.success)
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class HomebrewCaskPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "homebrew-cask"
    override val displayName: String = "Homebrew Cask"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Mac", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("brew")
    }

    override fun doExportInstalledPackages(): List<String> {
        val result = runCommand(listOf("brew", "list", "--cask"))
        check(result.success) { result.output.ifBlank { "读取 Homebrew Cask 失败" } }
        return parseLines(result.stdout)
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(
                managerId = managerId,
                displayName = displayName,
                output = "无缺失包",
            )
        }
        val result = runCommand(listOf("brew", "install", "--cask") + packages)
        return packageImportResult(packages, result.output, result.success)
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class WingetPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override val managerId: String = "winget"
    override val displayName: String = "Winget"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Windows", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("winget")
    }

    override fun doExportInstalledPackages(): List<String> {
        val exportFile = createTempFile("kbox-winget-export", ".json").toFile()
        return try {
            val result = runCommand(
                listOf(
                    "winget",
                    "export",
                    "-o",
                    exportFile.absolutePath,
                    "--accept-source-agreements",
                    "--disable-interactivity",
                ),
            )
            check(result.success) { result.output.ifBlank { "读取 Winget 列表失败" } }
            val jsonElement = json.parseToJsonElement(exportFile.readText())
            jsonElement.jsonObject["Sources"]
                ?.jsonArray
                ?.flatMap { sourceElement ->
                    sourceElement.jsonObject["Packages"]
                        ?.jsonArray
                        ?.mapNotNull { packageElement ->
                            packageElement.jsonObject["PackageIdentifier"]?.jsonPrimitive?.contentOrNull
                        }
                        .orEmpty()
                }
                .orEmpty()
        } finally {
            exportFile.delete()
        }
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(
                managerId = managerId,
                displayName = displayName,
                output = "无缺失包",
            )
        }
        val installed = mutableListOf<String>()
        val failed = mutableListOf<String>()
        val outputs = mutableListOf<String>()
        packages.forEach { packageId ->
            val result = runCommand(
                listOf(
                    "winget",
                    "install",
                    "--id",
                    packageId,
                    "--exact",
                    "--accept-package-agreements",
                    "--accept-source-agreements",
                    "--disable-interactivity",
                ),
            )
            outputs += "[${packageId}] ${result.output.ifBlank { if (result.success) "安装成功" else "安装失败" }}"
            if (result.success) {
                installed += packageId
            } else {
                failed += packageId
            }
        }
        return KboxPackageImportEntryResult(
            managerId = managerId,
            displayName = displayName,
            attemptedPackages = packages,
            installedPackages = installed,
            failedPackages = failed,
            output = outputs.joinToString(separator = "\n"),
        )
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class ChocolateyPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "chocolatey"
    override val displayName: String = "Chocolatey"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Windows", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("choco")
    }

    override fun doExportInstalledPackages(): List<String> {
        val result = runCommand(listOf("choco", "list", "--local-only", "--limit-output"))
        check(result.success) { result.output.ifBlank { "读取 Chocolatey 列表失败" } }
        return parseLines(result.stdout).map { line -> line.substringBefore('|') }
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(
                managerId = managerId,
                displayName = displayName,
                output = "无缺失包",
            )
        }
        val result = runCommand(listOf("choco", "install", "-y") + packages)
        return packageImportResult(packages, result.output, result.success)
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class AptPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "apt"
    override val displayName: String = "APT"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Linux", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("apt-mark") || commandExists("apt")
    }

    override fun doExportInstalledPackages(): List<String> {
        val result = runCommand(listOf("apt-mark", "showmanual"))
        check(result.success) { result.output.ifBlank { "读取 APT 列表失败" } }
        return parseLines(result.stdout)
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(managerId, displayName, output = "无缺失包")
        }
        val result = runCommand(elevatedCommand(listOf("apt-get", "install", "-y") + packages))
        return packageImportResult(packages, result.output, result.success)
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class DnfPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "dnf"
    override val displayName: String = "DNF"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Linux", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("dnf")
    }

    override fun doExportInstalledPackages(): List<String> {
        val primary = runCommand(listOf("dnf", "repoquery", "--userinstalled", "--qf", "%{name}"))
        if (primary.success) {
            return parseLines(primary.stdout)
        }
        val fallback = runCommand(listOf("dnf", "list", "installed"))
        check(fallback.success) { fallback.output.ifBlank { "读取 DNF 列表失败" } }
        return parseLines(fallback.stdout)
            .drop(1)
            .map { line -> line.substringBefore('.').substringBefore(' ') }
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(managerId, displayName, output = "无缺失包")
        }
        val result = runCommand(elevatedCommand(listOf("dnf", "install", "-y") + packages))
        return packageImportResult(packages, result.output, result.success)
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class PacmanPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "pacman"
    override val displayName: String = "Pacman"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Linux", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("pacman")
    }

    override fun doExportInstalledPackages(): List<String> {
        val result = runCommand(listOf("pacman", "-Qqe"))
        check(result.success) { result.output.ifBlank { "读取 Pacman 列表失败" } }
        return parseLines(result.stdout)
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(managerId, displayName, output = "无缺失包")
        }
        val result = runCommand(elevatedCommand(listOf("pacman", "-S", "--noconfirm") + packages))
        return packageImportResult(packages, result.output, result.success)
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class FlatpakPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "flatpak"
    override val displayName: String = "Flatpak"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Linux", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("flatpak")
    }

    override fun doExportInstalledPackages(): List<String> {
        val result = runCommand(listOf("flatpak", "list", "--app", "--columns=application"))
        check(result.success) { result.output.ifBlank { "读取 Flatpak 列表失败" } }
        return parseLines(result.stdout)
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(managerId, displayName, output = "无缺失包")
        }
        val result = runCommand(listOf("flatpak", "install", "-y", "flathub") + packages)
        return packageImportResult(packages, result.output, result.success)
    }
}

@Single(binds = [KboxPackageManagerAdapter::class])
class SnapPackageManagerAdapter(
    private val runner: KboxCommandRunner,
) : AbstractPackageManagerAdapter(runner) {
    override val managerId: String = "snap"
    override val displayName: String = "Snap"

    override fun supportsCurrentOs(): Boolean {
        return currentOsName.contains("Linux", ignoreCase = true)
    }

    override fun isAvailable(): Boolean {
        return commandExists("snap")
    }

    override fun doExportInstalledPackages(): List<String> {
        val result = runCommand(listOf("snap", "list"))
        check(result.success) { result.output.ifBlank { "读取 Snap 列表失败" } }
        return parseLines(result.stdout)
            .drop(1)
            .map { line -> line.substringBefore(' ') }
    }

    override fun installMissingPackages(
        packages: List<String>,
    ): KboxPackageImportEntryResult {
        if (packages.isEmpty()) {
            return KboxPackageImportEntryResult(managerId, displayName, output = "无缺失包")
        }
        val result = runCommand(elevatedCommand(listOf("snap", "install") + packages))
        return packageImportResult(packages, result.output, result.success)
    }
}
