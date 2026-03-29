package site.addzero.kbox.core

import kotlinx.serialization.json.Json
import site.addzero.kbox.core.model.KboxCommandResult
import site.addzero.kbox.core.model.KboxCommandSpec
import site.addzero.kbox.core.model.KboxComposeProjectAvailability
import site.addzero.kbox.core.service.KboxCommandRunner
import site.addzero.kbox.core.service.KboxComposeProjectService
import site.addzero.kbox.core.service.KboxPathService
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KboxComposeProjectServiceTest {
    @Test
    fun `register should discover compose services and build docker compose commands`() {
        val tempRoot = createTempDirectory("kbox-compose-project").toFile()
        val projectDir = File(tempRoot, "demo-project").apply {
            mkdirs()
        }
        File(projectDir, "compose.yaml").writeText(
            """
            services:
              web:
                image: nginx:latest
              db:
                image: postgres:16
            """.trimIndent(),
        )
        val commandRunner = FakeCommandRunner()
        val pathService = object : KboxPathService() {
            override fun defaultAppDataDir(): File {
                return File(tempRoot, "kbox-data")
            }
        }
        val service = KboxComposeProjectService(
            json = Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            },
            pathService = pathService,
            commandRunner = commandRunner,
        )

        service.registerProject(projectDir.absolutePath, "Demo")
        val project = service.listProjects().single()
        val validateResult = service.validateConfig(project.config.projectId)

        assertEquals("Demo", project.config.name)
        assertEquals(listOf("db", "web"), project.services)
        assertEquals(KboxComposeProjectAvailability.AVAILABLE, project.availability)
        assertTrue(validateResult.success)
        assertEquals(
            listOf("docker", "compose", "-f", "compose.yaml", "config"),
            commandRunner.commands.last().command,
        )
        assertEquals(projectDir.absolutePath, commandRunner.commands.last().workingDirectory)
    }

    private class FakeCommandRunner : KboxCommandRunner() {
        val commands = mutableListOf<KboxCommandSpec>()

        override fun isCommandAvailable(
            command: String,
        ): Boolean {
            return command == "docker"
        }

        override fun run(
            spec: KboxCommandSpec,
        ): KboxCommandResult {
            commands += spec
            return when (spec.command) {
                listOf("docker", "compose", "version") -> success(spec, "Docker Compose version v2")
                else -> success(spec, "ok")
            }
        }

        private fun success(
            spec: KboxCommandSpec,
            output: String,
        ) = KboxCommandResult(
            commandLine = spec.command.joinToString(" "),
            workingDirectory = spec.workingDirectory,
            exitCode = 0,
            stdout = output,
            stderr = "",
            timedOut = false,
            durationMillis = 1,
        )
    }
}
