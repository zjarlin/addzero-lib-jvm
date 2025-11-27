package site.addzero.gradle.plugin.kspbuddy
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

val GEN_DIR = "build-logic/src/main/kotlin/conventions/generated"

val extension = extensions.create<KspBuddyExtension>("kspBuddy")
extension.kspScriptOutputDir.convention(GEN_DIR)

val generateTask = tasks.register<GenerateKspScriptTask>("generateKspScript") {
    description = "Generates KSP configuration classes based on mustMap"
    group = "build"

    val outputDirPath = extension.kspScriptOutputDir.getOrElse(GEN_DIR)
    val generatedDir = File(project.rootProject.projectDir, outputDirPath)
    val moduleName = getModuleName(project)
    scriptOutputFile = File(generatedDir, "ksp4${moduleName}.gradle.kts")

    val buildOutputDir = project.layout.buildDirectory.dir("generated/ksp-buddy")
    generatedCodeOutputDir.set(buildOutputDir)

    mustMap.set(extension.mustMap)
    settingContextConfig.set(extension.settingContext)
    generateScript.set(extension.generatePrecompiledScript)
    targetProject.set(project)
}

afterEvaluate {
    tasks.findByName("compileKotlin")?.dependsOn(generateTask)
}

afterEvaluate {
    if (extension.mustMap.isPresent && extension.mustMap.get().isNotEmpty()
        && extension.generatePrecompiledScript.getOrElse(false)) {
        generateTask.get()
    }
}

fun getModuleName(project: Project): String {
    val path = project.path
    return if (path == ":") "root" else path.substring(1).replace(":", "-")
}

abstract class GenerateKspScriptTask : DefaultTask() {
    @get:Input
    abstract val mustMap: MapProperty<String, String>

    @get:Input
    abstract val settingContextConfig: Property<SettingContextConfig>

    @get:Input
    abstract val generateScript: Property<Boolean>

    @get:Input
    abstract val targetProject: Property<Project>

    @get:Internal
    abstract var scriptOutputFile: File

    @get:OutputDirectory
    abstract val generatedCodeOutputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        logger.lifecycle("KSP Buddy: Starting code generation...")
        logger.lifecycle("KSP Buddy: mustMap contains ${mustMap.get().size} entries")

        if (generateScript.get()) {
            scriptOutputFile.parentFile.mkdirs()
            val content = """
                |plugins {
                |    id("com.google.devtools.ksp")
                |}
                |
                |ksp {
                |${mustMap.get().map { (key, value) -> "    arg(\"$key\", \"$value\")" }.joinToString("\n")}
                |}
            """.trimMargin()
            scriptOutputFile.writeText(content)
            logger.lifecycle("Generated KSP configuration script to: ${scriptOutputFile.absolutePath}")
        } else {
            logger.lifecycle("KSP Buddy: Skipping gradle.kts script generation (generatePrecompiledScript is false)")
        }

        val generatedFiles = generateSettingsAndContext()

        val refreshHelper = project.objects.newInstance(GradleRefreshHelper::class.java)
        refreshHelper.requestGradleReevaluation(targetProject.get())
        refreshHelper.generateIdeRefreshHint(targetProject.get(), generatedFiles)
    }

    private fun generateSettingsAndContext(): List<File> {
        val config = settingContextConfig.get()
        if (!config.enabled) {
            logger.lifecycle("SettingContext generation is disabled")
            return emptyList()
        }

        val mustMapValue = mustMap.get()
        val outputDir = generatedCodeOutputDir.get().asFile
        val packageDir = File(outputDir, config.packageName.replace(".", "/"))
        packageDir.mkdirs()

        logger.lifecycle("Generating Settings and SettingContext in: ${packageDir.absolutePath}")
        logger.lifecycle("Package name: ${config.packageName}")
        logger.lifecycle("MustMap content: $mustMapValue")

        val generatedFiles = mutableListOf<File>()

        generateSettingsDataClass(packageDir, config.settingsClassName, mustMapValue, config.packageName)?.let { generatedFiles.add(it) }
        generateSettingContextObject(packageDir, config.contextClassName, config.settingsClassName, config.packageName, mustMapValue)?.let { generatedFiles.add(it) }

        return generatedFiles
    }

    private fun generateSettingsDataClass(packageDir: File, className: String, properties: Map<String, String>, packageName: String): File? {
        if (properties.isEmpty()) {
            logger.lifecycle("No properties provided, skipping Settings data class generation")
            return null
        }

        val file = File(packageDir, "${className}.kt")
        val propertyLines = properties.map { (key, value) -> "    val $key: String = \"$value\"" }.joinToString(",\n")
        val content = """
            |package $packageName
            |
            |data class $className(
            |$propertyLines
            |)
        """.trimMargin()

        file.writeText(content)
        logger.lifecycle("Generated Settings data class to: ${file.absolutePath}")
        return file
    }

    private fun generateSettingContextObject(packageDir: File, objectName: String, settingsClassName: String, packageName: String, properties: Map<String, String>): File? {
        if (properties.isEmpty()) {
            logger.lifecycle("No properties provided, skipping SettingContext object generation")
            return null
        }

        val file = File(packageDir, "${objectName}.kt")
        val propertyLines = properties.map { (key, _) -> "            $key = op[\"$key\"] ?: \"\"" }.joinToString(",\n")
        val content = """
            |package $packageName
            |
            |import java.util.concurrent.atomic.AtomicReference
            |
            |object $objectName {
            |    private val settingsRef = AtomicReference<$settingsClassName?>()
            |
            |    val settings: $settingsClassName
            |        get() = settingsRef.get() ?: $settingsClassName()
            |
            |    fun initialize(op: Map<String, String>) {
            |        val mapToBean = $settingsClassName(
            |$propertyLines
            |        )
            |        settingsRef.compareAndSet(null, mapToBean)
            |    }
            |}
        """.trimMargin()

        file.writeText(content)
        logger.lifecycle("Generated SettingContext object to: ${file.absolutePath}")
        return file
    }
}
