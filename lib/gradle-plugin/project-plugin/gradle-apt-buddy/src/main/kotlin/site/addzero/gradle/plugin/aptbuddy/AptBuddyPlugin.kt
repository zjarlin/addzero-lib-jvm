package site.addzero.gradle.plugin.aptbuddy

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

private const val GEN_DIR = "build-logic/src/main/kotlin/conventions/generated"

class AptBuddyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<AptBuddyExtension>("aptBuddy")
        extension.aptScriptOutputDir.convention(GEN_DIR)

        val generateTask = project.tasks.register<GenerateAptScriptTask>("generateAptScript") {
            description = "Generates APT configuration classes based on mustMap"
            group = "build"

            val outputDirPath = extension.aptScriptOutputDir.getOrElse(GEN_DIR)
            val generatedDir = File(project.rootProject.projectDir, outputDirPath)
            val moduleName = getModuleName(project)
            scriptOutputFile = File(generatedDir, "apt4${moduleName}.gradle.kts")

            val buildOutputDir = project.layout.buildDirectory.dir("generated/apt-buddy")
            generatedCodeOutputDir.set(buildOutputDir)
            mustMap.set(extension.mustMap)
            
            // 分解 settingContextConfig 为单独的属性
            contextClassName.set(extension.settingContext.map { it.contextClassName })
            settingsClassName.set(extension.settingContext.map { it.settingsClassName })
            packageName.set(extension.settingContext.map { it.packageName })
            settingContextEnabled.set(extension.settingContext.map { it.enabled })
            
            // 是否生成 precompiled script
            generateScript.set(extension.generatePrecompiledScript)
            
            projectPath.set(project.path)
        }

        project.afterEvaluate {
            try {
                val outputDir = generateTask.flatMap { it.generatedCodeOutputDir }
                
                // 如果启用了 settingContext 生成，添加生成的源码目录到源码集
                if (extension.settingContext.get().enabled && extension.mustMap.get().isNotEmpty()) {
                    project.plugins.withId("java") {
                        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
                        val mainSourceSet = sourceSets.getByName("main")
                        mainSourceSet.java.srcDir(outputDir)
                        project.logger.lifecycle("Added generated source directory to main sourceSet: ${outputDir.get().asFile.absolutePath}")
                    }
                }
                
                // 让 compileJava 依赖生成任务
                project.tasks.findByName("compileJava")?.let { compileTask ->
                    compileTask.dependsOn(generateTask)
                }
                
                // 让 compileKotlin 也依赖生成任务（如果存在）
                project.tasks.findByName("compileKotlin")?.let { compileTask ->
                    compileTask.dependsOn(generateTask)
                }
            } catch (e: Exception) {
                project.logger.warn("Failed to configure Java source sets: ${e.message}", e)
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
    abstract val contextClassName: Property<String>
    
    @get:org.gradle.api.tasks.Input
    abstract val settingsClassName: Property<String>
    
    @get:org.gradle.api.tasks.Input
    abstract val packageName: Property<String>
    
    @get:org.gradle.api.tasks.Input
    abstract val settingContextEnabled: Property<Boolean>
    
    @get:org.gradle.api.tasks.Input
    abstract val generateScript: Property<Boolean>

    @get:org.gradle.api.tasks.Input
    abstract val projectPath: Property<String>

    @get:org.gradle.api.tasks.Internal
    abstract var scriptOutputFile: File

    @get:OutputDirectory
    abstract val generatedCodeOutputDir: DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun generate() {
        logger.lifecycle("APT Buddy: Starting code generation...")
        logger.lifecycle("APT Buddy: mustMap contains ${mustMap.get().size} entries")
        
        // 只在 generateScript 为 true 时生成 gradle.kts 脚本文件
        if (generateScript.get()) {
            scriptOutputFile.parentFile.mkdirs()

            val content = buildString {
                appendLine("tasks.withType<JavaCompile> {")
                appendLine("    options.compilerArgs.addAll(listOf(")
                mustMap.get().forEach { (key, value) ->
                    appendLine("        \"-A$key=$value\",")
                }
                appendLine("    ))")
                appendLine("}")
            }

            scriptOutputFile.writeText(content)
            logger.lifecycle("Generated APT configuration script to: ${scriptOutputFile.absolutePath}")
        } else {
            logger.lifecycle("APT Buddy: Skipping gradle.kts script generation (generatePrecompiledScript is false)")
        }

        // 打印 Maven pom.xml 配置
        printMavenPomConfiguration()

        val generatedFiles = generateSettingsAndContext()
        logger.lifecycle("APT Buddy: Generated ${generatedFiles.size} configuration files")
        
        // 生成 IDE 刷新提示文件
        generateIdeRefreshHint(generatedFiles)
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
        if (!settingContextEnabled.get()) {
            logger.lifecycle("APT Buddy: SettingContext generation is disabled")
            return emptyList()
        }

        val outputDir = generatedCodeOutputDir.get().asFile
        val pkgName = packageName.get()
        val packageDir = File(outputDir, pkgName.replace(".", "/"))
        packageDir.mkdirs()

        logger.lifecycle("═══════════════════════════════════════════════════════════════")
        logger.lifecycle("📝 APT Buddy: Generating configuration classes")
        logger.lifecycle("═══════════════════════════════════════════════════════════════")
        logger.lifecycle("Output directory: ${packageDir.absolutePath}")
        logger.lifecycle("Package name: $pkgName")
        logger.lifecycle("Settings class: ${settingsClassName.get()}")
        logger.lifecycle("Context class: ${contextClassName.get()}")
        logger.lifecycle("Properties count: ${mustMap.get().size}")

        val generatedFiles = mutableListOf<File>()
        generateSettingsJavaClass(packageDir, settingsClassName.get(), mustMap.get(), pkgName)?.let { 
            generatedFiles.add(it)
            logger.lifecycle("✅ Generated: ${it.name}")
        }
        generateSettingContextJavaClass(packageDir, contextClassName.get(), settingsClassName.get(), pkgName, mustMap.get())?.let { 
            generatedFiles.add(it)
            logger.lifecycle("✅ Generated: ${it.name}")
        }
        
        logger.lifecycle("═══════════════════════════════════════════════════════════════")

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

    private fun generateIdeRefreshHint(generatedFiles: List<File>) {
        val outputDir = generatedCodeOutputDir.get().asFile
        val hintFile = File(outputDir, "apt-buddy-ide-hints.txt")
        hintFile.writeText(buildString {
            appendLine("APT Buddy IDE Refresh Hints")
            appendLine("============================")
            appendLine("Generated files that may need IDE refresh:")
            generatedFiles.forEach { file ->
                appendLine("- ${file.absolutePath}")
            }
            appendLine()
            appendLine("If you see compilation errors, try:")
            appendLine("1. Gradle -> Refresh Gradle Project (IntelliJ)")
            appendLine("2. File -> Reload Gradle Projects")
            appendLine("3. Restart IDE if necessary")
            appendLine()
            appendLine("Generated at: ${System.currentTimeMillis()}")
        })

        logger.lifecycle("Generated IDE refresh hints: ${hintFile.absolutePath}")
    }
}
