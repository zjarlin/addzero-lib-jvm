package site.addzero.gradle.tool

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ProjectUtilTest {
  @TempDir
  lateinit var rootDir: Path

  @Test
  fun `findAllProjectDirs skips generated and hidden directory trees`() {
    createProject("")
    createProject("apps/server")
    createProject("build-logic")
    createProject(".gradle/configuration-cache/generated")
    createProject(".idea/generated")
    createProject("build/generated")
    createProject("node_modules/dependency")
    createProject("out/generated")
    createProject("target/generated")

    val relativeProjectPaths = findAllProjectDirs(rootDir.toFile())
      .map { projectDir -> rootDir.relativize(projectDir.toPath()).toString() }
      .sorted()

    assertEquals(listOf("", "apps/server", "build-logic"), relativeProjectPaths)
  }

  private fun createProject(relativePath: String) {
    val projectDir = rootDir.resolve(relativePath)
    Files.createDirectories(projectDir)
    Files.write(projectDir.resolve("build.gradle.kts"), "plugins {}".toByteArray())
  }
}
