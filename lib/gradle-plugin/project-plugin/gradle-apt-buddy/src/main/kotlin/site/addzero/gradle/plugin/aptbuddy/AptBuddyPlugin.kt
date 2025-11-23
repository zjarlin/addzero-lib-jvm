package site.addzero.gradle.plugin.aptbuddy

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

private const val GEN_DIR = "build-logic/src/main/kotlin/conventions/generated"

class AptBuddyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<AptBuddyExtension>("aptBuddy")
        extension.aptScriptOutputDir.convention(GEN_DIR)

        val generateTask = project.tasks.register<GenerateAptScriptTask>("generateAptScript") {
            description = "Generates APT configuration script based on mustMap"
            group = "build"

            val outputDirPath = extension.aptScriptOutputDir.getOrElse(GEN_DIR)
            val generatedDir = File(project.rootProject.projectDir, outputDirPath)
            val moduleName = getModuleName(project)
            outputFile = File(generatedDir, "apt4${moduleName}.gradle.kts")

            val buildOutputDir = project.layout.buildDirectory.dir("generated/apt-buddy")
            generatedCodeOutputDir.set(buildOutputDir)
            mustMap.set(extension.mustMap)
            settingContextConfig.set(extension.settingContext)
            targetProject.set(project)
        }

        project.afterEvaluate {
            try {
                val outputDir = generateTask.flatMap { it.generatedCodeOutputDir }
                project.tasks.findByName("compileJava")?.let { compileTask ->
                    compileTask.dependsOn(generateTask)
                }
            } catch (e: Exception) {
                project.logger.warn("Failed to configure Java source sets: ${e.message}")
            }
        }

        project.afterEvaluate {
            if (extension.mustMap.isPresent && extension.mustMap.get().isNotEmpty()
                && extension.generatePrecompiledScript.getOrElse(false)) {
                generateTask.get()
            }
        }
    }

    private fun getModuleName(project: Project): String =
        project.path.takeIf { it != ":" }?.substring(1)?.replace(":", "-") ?: "root"
}

abstract class GenerateAptScriptTask : DefaultTask() {
    @get:org.gradle.api.tasks.Input
    abstract val mustMap: MapProperty<String, String>

    @get:org.gradle.api.tasks.Input
    abstract val settingContextConfig: Property<SettingContextConfig>

    @get:org.gradle.api.tasks.Input
    abstract val targetProject: Property<Project>

    @get:org.gradle.api.tasks.OutputFile
    abstract var outputFile: File

    @get:OutputDirectory
    abstract val generatedCodeOutputDir: DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun generate() {
        outputFile.parentFile.mkdirs()

        val content = buildString {
            appendLine("tasks.withType<JavaCompile> {")
            appendLine("    options.compilerArgs.addAll(listOf(")
            mustMap.get().forEach { (key, value) ->
                appendLine("        \"-A$key=$value\",")
            }
            appendLine("    ))")
            appendLine("}")
        }

        outputFile.writeText(content)
        logger.lifecycle("Generated APT configuration script to: ${outputFile.absolutePath}")

        // 打印 Maven pom.xml 配置
        printMavenPomConfiguration()

        val generatedFiles = generateSettingsAndContext()
        val refreshHelper = project.objects.newInstance(GradleRefreshHelper::class.java)
        refreshHelper.requestGradleReevaluation(targetProject.get())
        refreshHelper.generateIdeRefreshHint(targetProject.get(), generatedFiles)
    }

    private fun printMavenPomConfiguration() {
        logger.lifecycle("")
        logger.lifecycle("═══════════════════════════════════════════════════════════════")
        logger.lifecycle("📦 Maven pom.xml 配置等价项")
        logger.lifecycle("═══════════════════════════════════════════════════════════════")
        logger.lifecycle("")
        
        val pomConfig = buildString {
            appendLine("<properties>")
            mustMap.get().forEach { (key, value) ->
                val propertyName = key.replace(".", ".")
                appendLine("    <apt.$propertyName>$value</apt.$propertyName>")
            }
            appendLine("</properties>")
            appendLine()
            appendLine("<build>")
            appendLine("    <plugins>")
            appendLine("        <plugin>")
            appendLine("            <groupId>org.apache.maven.plugins</groupId>")
            appendLine("            <artifactId>maven-compiler-plugin</artifactId>")
            appendLine("            <configuration>")
            appendLine("                <compilerArgs>")
            mustMap.get().forEach { (key, value) ->
                val propertyRef = key.replace(".", ".")
                appendLine("                    <arg>-A$key=\${apt.$propertyRef}</arg>")
            }
            appendLine("                </compilerArgs>")
            appendLine("            </configuration>")
            appendLine("        </plugin>")
            appendLine("    </plugins>")
            appendLine("</build>")
        }
        
        pomConfig.lines().forEach { line ->
            logger.lifecycle(line)
        }
        
        logger.lifecycle("")
        logger.lifecycle("═══════════════════════════════════════════════════════════════")
        logger.lifecycle("")
    }

    private fun generateSettingsAndContext(): List<File> {
        val config = settingContextConfig.get()
        if (!config.enabled) {
            logger.lifecycle("SettingContext generation is disabled")
            return emptyList()
        }

        val outputDir = generatedCodeOutputDir.get().asFile
        val packageDir = File(outputDir, config.packageName.replace(".", "/"))
        packageDir.mkdirs()

        logger.lifecycle("Generating Settings and SettingContext in: ${packageDir.absolutePath}")
        logger.lifecycle("Package name: ${config.packageName}")

        val generatedFiles = mutableListOf<File>()
        generateSettingsJavaClass(packageDir, config.settingsClassName, mustMap.get(), config.packageName)?.let { generatedFiles.add(it) }
        generateSettingContextJavaClass(packageDir, config.contextClassName, config.settingsClassName, config.packageName, mustMap.get())?.let { generatedFiles.add(it) }

        return generatedFiles
    }

    private fun generateSettingsJavaClass(
        packageDir: File,
        className: String,
        properties: Map<String, String>,
        packageName: String
    ): File? {
        if (properties.isEmpty()) {
            logger.lifecycle("No properties provided, skipping Settings class generation")
            return null
        }

        val file = File(packageDir, "${className}.java")
        val fields = properties.map { (key, value) ->
            """    private String $key = "$value";"""
        }.joinToString("\n")

        val gettersSetters = properties.map { (key, _) ->
            val capitalizedKey = key.replaceFirstChar { it.uppercase() }
            """
    public String get$capitalizedKey() {
        return $key;
    }

    public void set$capitalizedKey(String $key) {
        this.$key = $key;
    }
            """.trimIndent()
        }.joinToString("\n\n")

        val constructorParams = properties.map { (key, _) -> "String $key" }.joinToString(", ")
        val constructorAssignments = properties.map { (key, _) -> "        this.$key = $key;" }.joinToString("\n")

        val content = """
package $packageName;

public class $className {
$fields

    public $className() {
    }

    public $className($constructorParams) {
$constructorAssignments
    }

$gettersSetters
}
        """.trimIndent()

        file.writeText(content)
        logger.lifecycle("Generated Settings Java class to: ${file.absolutePath}")
        return file
    }

    private fun generateSettingContextJavaClass(
        packageDir: File,
        className: String,
        settingsClassName: String,
        packageName: String,
        properties: Map<String, String>
    ): File? {
        if (properties.isEmpty()) {
            logger.lifecycle("No properties provided, skipping SettingContext class generation")
            return null
        }

        val file = File(packageDir, "${className}.java")
        val mapGets = properties.map { (key, _) ->
            """            settings.set${key.replaceFirstChar { it.uppercase() }}(op.getOrDefault("$key", ""));"""
        }.joinToString("\n")

        val content = """
package $packageName;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class $className {
    private static final AtomicReference<$settingsClassName> settingsRef = new AtomicReference<>();

    public static $settingsClassName getSettings() {
        $settingsClassName settings = settingsRef.get();
        return settings != null ? settings : new $settingsClassName();
    }

    public static void initialize(Map<String, String> op) {
        $settingsClassName settings = new $settingsClassName();
$mapGets
        settingsRef.compareAndSet(null, settings);
    }
}
        """.trimIndent()

        file.writeText(content)
        logger.lifecycle("Generated SettingContext Java class to: ${file.absolutePath}")
        return file
    }
}
