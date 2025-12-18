package site.addzero.gradle.plugin

val GEN_DIR = "build-logic/src/main/kotlin/conventions/generated"

val extension = extensions.create<AptBuddyExtension>("aptBuddy")
extension.aptScriptOutputDir.convention(GEN_DIR)

val generateTask = tasks.register<GenerateAptScriptTask>("generateAptScript") {
    description = "Generates APT configuration classes based on mustMap"
    group = "build"

    val outputDirPath = extension.aptScriptOutputDir.getOrElse(GEN_DIR)
    val generatedDir = File(project.rootProject.projectDir, outputDirPath)
    val moduleName = getModuleName(project)
    scriptOutputFile = File(generatedDir, "apt4${moduleName}.gradle.kts")

    val buildOutputDir = project.layout.buildDirectory.dir("generated/apt-buddy")
    generatedCodeOutputDir.set(buildOutputDir)
    mustMap.set(extension.mustMap)

    contextClassName.set(extension.contextClassName)
    settingsClassName.set(extension.settingsClassName)
    packageName.set(extension.packageName)
    settingContextEnabled.set(extension.settingContextEnabled)
    generateScript.set(extension.generatePrecompiledScript)
    projectPath.set(project.path)
}

afterEvaluate {
    val outputDir = generateTask.flatMap { it.generatedCodeOutputDir }

    if (extension.settingContextEnabled.get() && extension.mustMap.get().isNotEmpty()) {
        plugins.withId("java") {
            val sourceSets = extensions.getByType(SourceSetContainer::class.java)
            sourceSets.getByName("main").java.srcDir(outputDir)
            logger.lifecycle("Added generated source directory to main sourceSet: ${outputDir.get().asFile.absolutePath}")
        }
    }

    tasks.findByName("compileJava")?.dependsOn(generateTask)
    tasks.findByName("compileKotlin")?.dependsOn(generateTask)
}

fun getModuleName(project: Project): String =
    project.path.takeIf { it != ":" }?.substring(1)?.replace(":", "-") ?: "root"

abstract class GenerateAptScriptTask : DefaultTask() {
    @get:Input
    abstract val mustMap: MapProperty<String, String>

    @get:Input
    abstract val contextClassName: Property<String>

    @get:Input
    abstract val settingsClassName: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val settingContextEnabled: Property<Boolean>

    @get:Input
    abstract val generateScript: Property<Boolean>

    @get:Input
    abstract val projectPath: Property<String>

    @get:Internal
    abstract var scriptOutputFile: File

    @get:OutputDirectory
    abstract val generatedCodeOutputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        logger.lifecycle("APT Buddy: Starting code generation...")
        logger.lifecycle("APT Buddy: mustMap contains ${mustMap.get().size} entries")

        if (generateScript.get()) {
            scriptOutputFile.parentFile.mkdirs()
            val content = """
                |tasks.withType<JavaCompile> {
                |    options.compilerArgs.addAll(listOf(
                |${mustMap.get().map { (key, value) -> "        \"-A$key=$value\"," }.joinToString("\n")}
                |    ))
                |}
            """.trimMargin()
            scriptOutputFile.writeText(content)
            logger.lifecycle("Generated APT configuration script to: ${scriptOutputFile.absolutePath}")
        } else {
            logger.lifecycle("APT Buddy: Skipping gradle.kts script generation (generatePrecompiledScript is false)")
        }

        printMavenPomConfiguration()
        val generatedFiles = generateSettingsAndContext()
        logger.lifecycle("APT Buddy: Generated ${generatedFiles.size} configuration files")
        generateIdeRefreshHint(generatedFiles)
    }

    private fun printMavenPomConfiguration() {
        logger.lifecycle("")
        logger.lifecycle("═══════════════════════════════════════════════════════════════")
        logger.lifecycle("📦 Maven pom.xml 配置等价项")
        logger.lifecycle("═══════════════════════════════════════════════════════════════")
        logger.lifecycle("")

        val pomConfig = """
            |<properties>
            |${mustMap.get().map { (key, value) -> "    <apt.$key>$value</apt.$key>" }.joinToString("\n")}
            |</properties>
            |
            |<build>
            |    <plugins>
            |        <plugin>
            |            <groupId>org.apache.maven.plugins</groupId>
            |            <artifactId>maven-compiler-plugin</artifactId>
            |            <configuration>
            |                <compilerArgs>
            |${mustMap.get().map { (key, _) -> "                    <arg>-A$key=\${apt.$key}</arg>" }.joinToString("\n")}
            |                </compilerArgs>
            |            </configuration>
            |        </plugin>
            |    </plugins>
            |</build>
        """.trimMargin()

        pomConfig.lines().forEach { logger.lifecycle(it) }
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
        generateSettingContextJavaClass(
            packageDir,
            contextClassName.get(),
            settingsClassName.get(),
            pkgName,
            mustMap.get()
        )?.let {
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
        if (properties.isEmpty()) return null

        val file = File(packageDir, "${className}.java")
        val fields = properties.map { (key, value) -> """    private String $key = "$value";""" }.joinToString("\n")
        val gettersSetters = properties.map { (key, _) ->
            val cap = key.replaceFirstChar { it.uppercase() }
            """
    public String get$cap() {
        return $key;
    }

    public void set$cap(String $key) {
        this.$key = $key;
    }"""
        }.joinToString("\n")
        val constructorParams = properties.map { (key, _) -> "String $key" }.joinToString(", ")
        val constructorAssignments = properties.map { (key, _) -> "        this.$key = $key;" }.joinToString("\n")

        val content = """
            |package $packageName;
            |
            |public class $className {
            |$fields
            |
            |    public $className() {
            |    }
            |
            |    public $className($constructorParams) {
            |$constructorAssignments
            |    }
            |$gettersSetters
            |}
        """.trimMargin()

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
        if (properties.isEmpty()) return null

        val file = File(packageDir, "${className}.java")
        val mapGets = properties.map { (key, _) ->
            """            settings.set${key.replaceFirstChar { it.uppercase() }}(op.getOrDefault("$key", ""));"""
        }.joinToString("\n")

        val content = """
            |package $packageName;
            |
            |import java.util.Map;
            |import java.util.concurrent.atomic.AtomicReference;
            |
            |public class $className {
            |    private static final AtomicReference<$settingsClassName> settingsRef = new AtomicReference<>();
            |
            |    public static $settingsClassName getSettings() {
            |        $settingsClassName settings = settingsRef.get();
            |        return settings != null ? settings : new $settingsClassName();
            |    }
            |
            |    public static void initialize(Map<String, String> op) {
            |        $settingsClassName settings = new $settingsClassName();
            |$mapGets
            |        settingsRef.compareAndSet(null, settings);
            |    }
            |}
        """.trimMargin()

        file.writeText(content)
        logger.lifecycle("Generated SettingContext Java class to: ${file.absolutePath}")
        return file
    }

    private fun generateIdeRefreshHint(generatedFiles: List<File>) {
        val outputDir = generatedCodeOutputDir.get().asFile
        val hintFile = File(outputDir, "apt-buddy-ide-hints.txt")
        hintFile.writeText(
            """
            |APT Buddy IDE Refresh Hints
            |============================
            |Generated files that may need IDE refresh:
            |${generatedFiles.joinToString("\n") { "- ${it.absolutePath}" }}
            |
            |If you see compilation errors, try:
            |1. Gradle -> Refresh Gradle Project (IntelliJ)
            |2. File -> Reload Gradle Projects
            |3. Restart IDE if necessary
            |
            |Generated at: ${System.currentTimeMillis()}
        """.trimMargin()
        )
        logger.lifecycle("Generated IDE refresh hints: ${hintFile.absolutePath}")
    }
}
