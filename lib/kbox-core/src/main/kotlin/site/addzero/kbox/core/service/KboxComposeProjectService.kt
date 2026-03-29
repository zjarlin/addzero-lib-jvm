package site.addzero.kbox.core.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.yaml.snakeyaml.Yaml
import site.addzero.kbox.core.model.KboxCommandResult
import site.addzero.kbox.core.model.KboxCommandSpec
import site.addzero.kbox.core.model.KboxComposeProjectAvailability
import site.addzero.kbox.core.model.KboxComposeProjectConfig
import site.addzero.kbox.core.model.KboxComposeProjectFiles
import site.addzero.kbox.core.model.KboxComposeProjectSnapshot
import site.addzero.kbox.core.support.stableShortHash
import java.io.File

@Single
class KboxComposeProjectService(
    private val json: Json,
    private val pathService: KboxPathService,
    private val commandRunner: KboxCommandRunner,
) {
    private val yaml = Yaml()

    fun listProjects(): List<KboxComposeProjectSnapshot> {
        return readRegistry()
            .sortedBy { config -> config.name.lowercase() }
            .map { config ->
                val availability = detectComposeAvailability()
                KboxComposeProjectSnapshot(
                    config = config,
                    services = parseServiceNames(config),
                    availability = availability.first,
                    availabilityMessage = availability.second,
                )
            }
    }

    fun registerProject(
        directory: String,
        name: String = "",
    ): KboxComposeProjectConfig {
        val projectDir = File(directory.trim()).absoluteFile
        require(projectDir.isDirectory) {
            "Compose 项目目录不存在：${projectDir.absolutePath}"
        }
        val composeFiles = discoverComposeFiles(projectDir)
        require(composeFiles.isNotEmpty()) {
            "目录内未发现 compose.yaml / compose.yml / docker-compose*.yml"
        }
        val config = KboxComposeProjectConfig(
            projectId = stableShortHash(projectDir.absolutePath),
            name = name.trim().ifBlank { projectDir.name },
            directory = projectDir.absolutePath,
            composeFiles = composeFiles,
            envFile = File(projectDir, ".env").absolutePath,
            enabled = true,
        )
        saveRegistry(
            readRegistry()
                .filterNot { saved -> saved.projectId == config.projectId || saved.directory == config.directory }
                .plus(config),
        )
        return config
    }

    fun removeProject(
        projectId: String,
    ) {
        saveRegistry(
            readRegistry().filterNot { config -> config.projectId == projectId },
        )
    }

    fun updateProject(
        config: KboxComposeProjectConfig,
    ) {
        saveRegistry(
            readRegistry()
                .filterNot { saved -> saved.projectId == config.projectId }
                .plus(config),
        )
    }

    fun readProjectFiles(
        projectId: String,
    ): KboxComposeProjectFiles {
        val project = requireProject(projectId)
        val composeContent = project.composeFiles.associateWith { fileName ->
            File(project.directory, fileName).takeIf(File::isFile)?.readText().orEmpty()
        }
        val envContent = File(project.envFile).takeIf(File::isFile)?.readText().orEmpty()
        return KboxComposeProjectFiles(
            composeFileContent = composeContent,
            envContent = envContent,
        )
    }

    fun saveComposeFile(
        projectId: String,
        composeFile: String,
        content: String,
    ) {
        val project = requireProject(projectId)
        require(project.composeFiles.contains(composeFile)) {
            "Compose 文件未注册：$composeFile"
        }
        val targetFile = File(project.directory, composeFile)
        targetFile.parentFile?.mkdirs()
        targetFile.writeText(content)
    }

    fun saveEnvFile(
        projectId: String,
        content: String,
    ) {
        val project = requireProject(projectId)
        val targetFile = File(project.envFile)
        targetFile.parentFile?.mkdirs()
        targetFile.writeText(content)
    }

    fun validateConfig(
        projectId: String,
    ): KboxCommandResult {
        return runComposeCommand(projectId, listOf("config"))
    }

    fun upDetached(
        projectId: String,
    ): KboxCommandResult {
        return runComposeCommand(projectId, listOf("up", "-d"))
    }

    fun down(
        projectId: String,
    ): KboxCommandResult {
        return runComposeCommand(projectId, listOf("down"))
    }

    fun restart(
        projectId: String,
    ): KboxCommandResult {
        return runComposeCommand(projectId, listOf("restart"))
    }

    fun pull(
        projectId: String,
    ): KboxCommandResult {
        return runComposeCommand(projectId, listOf("pull"))
    }

    fun logs(
        projectId: String,
    ): KboxCommandResult {
        return runComposeCommand(projectId, listOf("logs", "--tail", "200"))
    }

    fun ps(
        projectId: String,
    ): KboxCommandResult {
        return runComposeCommand(projectId, listOf("ps"))
    }

    fun detectComposeAvailability(): Pair<KboxComposeProjectAvailability, String> {
        resolveComposeCommand()?.let {
            return KboxComposeProjectAvailability.AVAILABLE to "可用"
        }
        return KboxComposeProjectAvailability.UNAVAILABLE to "未检测到 docker compose / docker-compose"
    }

    private fun runComposeCommand(
        projectId: String,
        args: List<String>,
    ): KboxCommandResult {
        val project = requireProject(projectId)
        val composeCommand = resolveComposeCommand()
            ?: error("当前系统不可用 docker compose / docker-compose")
        val command = composeCommand.baseCommand
            .plus(project.composeFiles.flatMap { fileName -> listOf("-f", fileName) })
            .plus(
                File(project.envFile)
                    .takeIf { file -> file.isFile }
                    ?.let { file -> listOf("--env-file", file.absolutePath) }
                    .orEmpty(),
            )
            .plus(args)
        return commandRunner.run(
            KboxCommandSpec(
                command = command,
                workingDirectory = project.directory,
            ),
        )
    }

    private fun parseServiceNames(
        project: KboxComposeProjectConfig,
    ): List<String> {
        return project.composeFiles.asSequence()
            .map { fileName -> File(project.directory, fileName) }
            .filter(File::isFile)
            .flatMap { file ->
                val parsed = runCatching {
                    yaml.load<Any>(file.readText())
                }.getOrNull()
                val services = (parsed as? Map<*, *>)?.get("services") as? Map<*, *>
                services?.keys.orEmpty().asSequence().map { key -> key.toString() }
            }
            .distinct()
            .sorted()
            .toList()
    }

    private fun discoverComposeFiles(
        projectDir: File,
    ): List<String> {
        return listOf(
            "compose.yaml",
            "compose.yml",
            "docker-compose.yml",
            "docker-compose.yaml",
        ).filter { fileName -> File(projectDir, fileName).isFile }
    }

    private fun requireProject(
        projectId: String,
    ): KboxComposeProjectConfig {
        return readRegistry().firstOrNull { config -> config.projectId == projectId }
            ?: error("未找到 Compose 项目：$projectId")
    }

    private fun readRegistry(): List<KboxComposeProjectConfig> {
        val registryFile = pathService.composeProjectsRegistryFile()
        if (!registryFile.isFile) {
            return emptyList()
        }
        return json.decodeFromString<List<KboxComposeProjectConfig>>(registryFile.readText())
    }

    private fun saveRegistry(
        projects: List<KboxComposeProjectConfig>,
    ) {
        val registryFile = pathService.composeProjectsRegistryFile()
        registryFile.parentFile?.mkdirs()
        registryFile.writeText(json.encodeToString(projects.sortedBy { project -> project.name.lowercase() }))
    }

    private fun resolveComposeCommand(): ComposeCommand? {
        if (commandRunner.isCommandAvailable("docker")) {
            val result = commandRunner.run(
                KboxCommandSpec(
                    command = listOf("docker", "compose", "version"),
                    timeoutMillis = 15_000,
                ),
            )
            if (result.success) {
                return ComposeCommand(baseCommand = listOf("docker", "compose"))
            }
        }
        if (commandRunner.isCommandAvailable("docker-compose")) {
            val result = commandRunner.run(
                KboxCommandSpec(
                    command = listOf("docker-compose", "version"),
                    timeoutMillis = 15_000,
                ),
            )
            if (result.success) {
                return ComposeCommand(baseCommand = listOf("docker-compose"))
            }
        }
        return null
    }

    private data class ComposeCommand(
        val baseCommand: List<String>,
    )
}
