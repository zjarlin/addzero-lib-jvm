import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText

enum class DependencyBucket {
    Main,
    Test,
    JavaProcessor,
    JavaTestProcessor,
    KspProcessor,
    KspTestProcessor,
}

data class Dependency(
    val notation: String,
    val exported: Boolean = false,
    val scope: String = "all",
    val bom: Boolean = false,
)

data class SettingsGeneration(
    val packageName: String,
    val properties: LinkedHashMap<String, String>,
    val style: String = "context",
    val settingsClassName: String = "Settings",
    val contextClassName: String = "SettingContext",
)

data class ModuleModel(
    val gradleFile: Path,
    val sourceDirectory: Path,
    val configDirectory: Path,
    val moduleName: String,
    val artifactId: String,
    val description: String,
    val templates: List<String>,
    val mainDependencies: List<Dependency>,
    val testDependencies: List<Dependency>,
    val javaProcessors: List<String>,
    val javaTestProcessors: List<String>,
    val kspProcessors: List<String>,
    val kspTestProcessors: List<String>,
    val kspProcessorOptions: LinkedHashMap<String, String>,
    val compilerPlugins: List<Pair<String, String>>,
    val settingsGeneration: SettingsGeneration?,
    val kotlinVersion: String?,
    val jdkVersion: Int,
    val publishingEnabled: Boolean,
)

data class KmpModuleModel(
    val gradleFile: Path,
    val sourceDirectory: Path,
    val configDirectory: Path,
    val moduleName: String,
    val artifactId: String,
    val description: String,
    val platforms: List<String>,
    val mainDependencies: Map<String, List<Dependency>>,
    val testDependencies: Map<String, List<Dependency>>,
    val kspProcessors: Map<String, List<String>>,
    val settingsGeneration: SettingsGeneration?,
    val composeEnabled: Boolean,
    val serializationEnabled: Boolean,
    val koinCompilerEnabled: Boolean,
    val compilerPlugins: List<Pair<String, String>>,
    val jvmRelease: Int,
    val publishingEnabled: Boolean,
)

data class DependencyCall(
    val configuration: String,
    val expression: String,
    val position: Int,
)

data class SupportModule(
    val sourceDirectories: List<Path>,
    val configDirectory: Path,
    val moduleName: String,
    val artifactId: String,
    val description: String,
    val templates: List<String>,
    val dependencies: List<Dependency>,
)

val root = Path.of(".").toAbsolutePath().normalize()
val repositoryRoot = root
val libDirectory = root.resolve("lib")
val signArtifacts = System.getenv("ADDZERO_SIGN_ARTIFACTS").toBoolean()
val publicationVersion = LocalDate.now()
    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"))
require(libDirectory.isDirectory()) { "Run this script from the addzero-lib-jvm repository root" }

val catalogFile = root.resolve("libs.versions.toml")
require(catalogFile.exists()) { "Missing libs.versions.toml; copy the authoritative build-logic catalog first" }

val catalogAliasesByModule = parseCatalogAliasesByModule(catalogFile.readText())
val allGradleFiles = Files.walk(libDirectory).use { paths ->
    paths.filter { path -> path.fileName.toString() == "build.gradle.kts" }
        .filter { path -> !path.toString().contains("/build/") }
        .sorted()
        .toList()
}

val exclusions = linkedMapOf<Path, String>()
val candidates = allGradleFiles.filter { gradleFile ->
    val relative = gradleFile.relativeTo(root).toString()
    val content = stripComments(gradleFile.readText())
    val reason = exclusionReason(relative, content)
    if (reason != null) {
        exclusions[gradleFile] = reason
        false
    } else {
        true
    }
}

val blockedKmpFiles = setOf(
    root.resolve("lib/compose/compose-native-component-sheet/build.gradle.kts"),
)
blockedKmpFiles.forEach { file -> exclusions[file] = "missing-source-dependency" }
val kmpGradleFiles = exclusions.entries
    .filter { (_, reason) -> reason == "kmp-library" }
    .map(Map.Entry<Path, String>::key)
val migratedGradleFiles = candidates + kmpGradleFiles
val migratedDirectories = migratedGradleFiles.map(Path::getParent).toSet()
val commonModelsDirectory = root.resolve("lib/tool-kmp/models/common/common-models")
val genReifiedCoreDirectory = root.resolve("lib/ksp/metadata/gen-reified/gen-reified-core")
val jdbcModelDirectory = root.resolve("lib/tool-kmp/jdbc/tool-jdbc-model")
val toolJsonDirectory = root.resolve("lib/tool-kmp/tool-json")
val starterSpiDirectory = root.resolve("lib/tool-kmp/ktor/starter/starter-spi")
val loggerSampleAppDirectory = root.resolve("lib/ksp/app")
val supportModules = listOf(
    SupportModule(
        sourceDirectories = listOf(starterSpiDirectory.resolve("src/main/kotlin")),
        configDirectory = starterSpiDirectory,
        moduleName = "starter-spi",
        artifactId = "starter-spi",
        description = "Ktor application starter SPI",
        templates = listOf(
            "//build-config/jvm-lib.module-template.yaml",
            "//build-config/jvm8.module-template.yaml",
        ),
        dependencies = listOf(Dependency(catalogReference("io-ktor-ktor-server-core"))),
    ),
)
val supportModuleNotations = supportModules.associate { supportModule ->
    when (supportModule.moduleName) {
        "starter-spi" -> starterSpiDirectory
        else -> error("Unsupported support module ${supportModule.moduleName}")
    } to "//${supportModule.configDirectory.relativeTo(root)}"
}
val overlayRoot = root.resolve("toolchain/modules")
val kmpProjectRoot = root.resolve("toolchain/kmp-project")
val kmpOverlayRoot = kmpProjectRoot.resolve("modules")
overlayRoot.createDirectories()
kmpOverlayRoot.createDirectories()
val duplicateNames = migratedGradleFiles.groupBy { it.parent.name }
    .filterValues { files -> files.size > 1 }
    .keys
val moduleNamesByDirectory = migratedDirectories.associateWith { directory ->
    moduleNameFor(directory, libDirectory, duplicateNames)
}
val configDirectoriesBySource = migratedDirectories.associateWith { directory ->
    val moduleName = moduleNamesByDirectory.getValue(directory)
    when {
        directory in kmpGradleFiles.map(Path::getParent).toSet() -> kmpOverlayRoot.resolve(moduleName)
        directory.toString().contains("/lib/tool-jvm/yudao3/") && directory.name in duplicateNames ->
            overlayRoot.resolve(moduleName)
        else -> directory
    }
}
val localModuleNotations = configDirectoriesBySource.mapValues { (_, configDirectory) ->
    "//${configDirectory.relativeTo(root)}"
} + supportModuleNotations
val kmpDirectories = kmpGradleFiles.map(Path::getParent).toSet()
val localJvmModuleNotations = localModuleNotations.filterKeys { directory -> directory !in kmpDirectories }
val localKmpModuleNotations = kmpDirectories.associateWith { directory ->
    "//modules/${moduleNamesByDirectory.getValue(directory)}"
}
val catalogCoordinatesByAlias = catalogAliasesByModule.entries.associate { (coordinate, alias) -> alias to coordinate }
val localLibraryAliasDirectories = buildMap {
    migratedDirectories.forEach { directory ->
        val catalogCoordinate = catalogCoordinatesByAlias[directory.name]
        if (catalogCoordinate == null || catalogCoordinate.startsWith("site.addzero:")) {
            putIfAbsent(directory.name, directory)
        }
        putIfAbsent("site-addzero-${directory.name}", directory)
    }
}
val projectAccessorToDirectory = allGradleFiles.associate { gradleFile ->
    val directory = gradleFile.parent
    directory.toGradleProjectAccessor() to directory
}
val gradlePathToDirectory = allGradleFiles.associate { gradleFile ->
    val directory = gradleFile.parent
    directory.toGradleProjectPath() to directory
} + supportModuleNotations.keys.associate { directory -> directory.toGradleProjectPath() to directory }
val publicationOwnersByArtifact = migratedDirectories.groupBy(Path::getFileName)
    .mapValues { (_, directories) ->
        directories.sortedWith(
            compareBy<Path> { directory -> directory.toString().contains("/lib/tool-jvm/yudao3/") }
                .thenBy(Path::toString),
        ).first()
    }

val unresolved = mutableListOf<String>()
val models = candidates.map { gradleFile ->
    val sourceDirectory = gradleFile.parent
    val relative = gradleFile.relativeTo(root).toString()
    val moduleName = moduleNamesByDirectory.getValue(sourceDirectory)
    val configDirectory = configDirectoriesBySource.getValue(sourceDirectory)
    val content = stripComments(gradleFile.readText())
    val variableStrings = parseStringVariables(content)

    val dependencies = linkedMapOf<DependencyBucket, MutableList<Dependency>>()
    DependencyBucket.entries.forEach { bucket -> dependencies[bucket] = mutableListOf() }

    extractDependencyCalls(content).forEach { (configuration, expression) ->
        val bucket = configuration.toBucket()
        val dependency = parseDependency(
            expression = expression,
            configuration = configuration,
            root = root,
            projectAccessorToDirectory = projectAccessorToDirectory,
            gradlePathToDirectory = gradlePathToDirectory,
            catalogAliasesByModule = catalogAliasesByModule,
            variables = variableStrings,
            localModuleNotations = localJvmModuleNotations,
            localLibraryAliasDirectories = localLibraryAliasDirectories,
            projectVersionForNonLocal = publicationVersion,
        )

        if (dependency == null) {
            if (!isIgnorableDependency(expression)) {
                unresolved += "${gradleFile.relativeTo(root)}: $configuration($expression)"
            }
        } else {
            dependencies.getValue(bucket) += dependency
        }
    }

    val settingsGeneration = processorSettingsFor(
        relative = relative,
        content = content,
    )
    val kotlinVersion = kotlinCompilerPluginVersionFor(relative)
    if (kotlinVersion != null) {
        dependencies.values.forEach { bucket ->
            bucket.replaceAll { dependency ->
                if (dependency.notation == catalogReference("org-jetbrains-kotlin-kotlin-compiler-embeddable")) {
                    dependency.copy(notation = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
                } else {
                    dependency
                }
            }
        }
    }
    val description = parseDescription(content)
        ?: sourceDirectory.resolve("README.md").takeIf(Path::exists)?.readFirstMeaningfulLine()
        ?: "site.addzero:${sourceDirectory.name}"
    val publishingEnabled = !sourceDirectory.name.endsWith("-smoke") &&
        sourceDirectory.name != "published-gradle-plugin-tests" &&
        publicationOwnersByArtifact[sourceDirectory.fileName] == sourceDirectory

    ModuleModel(
        gradleFile = gradleFile,
        sourceDirectory = sourceDirectory,
        configDirectory = configDirectory,
        moduleName = moduleName,
        artifactId = sourceDirectory.name,
        description = description,
        templates = templatesFor(content),
        mainDependencies = dependencies.getValue(DependencyBucket.Main).distinct(),
        testDependencies = dependencies.getValue(DependencyBucket.Test).distinct(),
        javaProcessors = dependencies.getValue(DependencyBucket.JavaProcessor).map(Dependency::notation).distinct(),
        javaTestProcessors = dependencies.getValue(DependencyBucket.JavaTestProcessor).map(Dependency::notation).distinct(),
        kspProcessors = dependencies.getValue(DependencyBucket.KspProcessor).map(Dependency::notation).distinct(),
        kspTestProcessors = dependencies.getValue(DependencyBucket.KspTestProcessor).map(Dependency::notation).distinct(),
        kspProcessorOptions = kspProcessorOptionsFor(relative),
        compilerPlugins = compilerPluginsFor(relative, content),
        settingsGeneration = settingsGeneration,
        kotlinVersion = kotlinVersion,
        jdkVersion = if (content.contains("JavaLanguageVersion.of(17)")) 17 else 8,
        publishingEnabled = publishingEnabled,
    )
}

val kmpConventionDirectory = root.resolve("checkouts/build-logic/src/main/kotlin/site/addzero/buildlogic/kmp")
val kmpConventionFiles = Files.list(kmpConventionDirectory).use { paths ->
    paths.filter { path -> path.fileName.toString().endsWith(".gradle.kts") }
        .toList()
        .associateBy { path -> path.fileName.toString().removeSuffix(".gradle.kts") }
}
val kmpModels = kmpGradleFiles.map { gradleFile ->
    val sourceDirectory = gradleFile.parent
    val moduleName = moduleNamesByDirectory.getValue(sourceDirectory)
    val configDirectory = configDirectoriesBySource.getValue(sourceDirectory)
    val moduleContent = stripComments(gradleFile.readText())
    val buildSources = resolveKmpBuildSources(moduleContent, kmpConventionFiles)
    val combinedContent = buildSources.joinToString("\n")
    val variables = buildSources.flatMap { source -> parseStringVariables(source).entries }
        .associate(Map.Entry<String, String>::toPair)
    val mainDependencies = linkedMapOf<String, MutableList<Dependency>>()
    val testDependencies = linkedMapOf<String, MutableList<Dependency>>()
    val kspProcessors = linkedMapOf<String, MutableList<String>>()

    buildSources.forEach { source ->
        extractDependencyCallsWithPositions(source).forEach { call ->
            if (call.expression.contains("org-jetbrains-kotlin-kotlin-test")) {
                return@forEach
            }
            val qualifier = dependencyQualifier(source, call)
            val normalizedConfiguration = normalizeKmpConfiguration(call.configuration)
            val dependency = parseDependency(
                expression = call.expression,
                configuration = normalizedConfiguration,
                root = root,
                projectAccessorToDirectory = projectAccessorToDirectory,
                gradlePathToDirectory = gradlePathToDirectory,
                catalogAliasesByModule = catalogAliasesByModule,
                variables = variables,
                localModuleNotations = localKmpModuleNotations,
                localLibraryAliasDirectories = localLibraryAliasDirectories,
                projectVersionForNonLocal = publicationVersion,
            )
            if (dependency == null) {
                if (!isIgnorableDependency(call.expression)) {
                    unresolved += "${gradleFile.relativeTo(root)}: ${call.configuration}(${call.expression})"
                }
                return@forEach
            }
            when {
                call.configuration.startsWith("ksp", ignoreCase = true) ->
                    kspProcessors.getOrPut(qualifier) { mutableListOf() } += dependency.notation
                isTestDependency(source, call) ->
                    testDependencies.getOrPut(qualifier) { mutableListOf() } += dependency
                else -> mainDependencies.getOrPut(qualifier) { mutableListOf() } += dependency
            }
        }
    }
    val logbackDependency = catalogReference("ch-qos-logback-logback-classic")
    mainDependencies[""]?.removeAll { dependency ->
        if (dependency.notation != logbackDependency) {
            return@removeAll false
        }
        mainDependencies.getOrPut("jvm") { mutableListOf() } += dependency
        true
    }
    if (sourceDirectory.endsWith("lib/tool-kmp/network-starter")) {
        mainDependencies.getOrPut("android") { mutableListOf() } +=
            Dependency(catalogReference("io-ktor-ktor-client-cio"), exported = true)
    }
    if (combinedContent.contains("site.addzero.kcp.spread-pack")) {
        val annotationsDirectory = root.resolve("lib/kcp/spread-pack/kcp-spread-pack-annotations")
        mainDependencies.getOrPut("") { mutableListOf() } +=
            Dependency(localKmpModuleNotations.getValue(annotationsDirectory))
    }
    if (combinedContent.contains("site.addzero.buildlogic.kmp.kmp-ksp-plugin")) {
        mainDependencies.getOrPut("") { mutableListOf() } +=
            Dependency(catalogReference("com-google-devtools-ksp-symbol-processing-api"))
    }

    val description = parseDescription(moduleContent)
        ?: sourceDirectory.resolve("README.md").takeIf(Path::exists)?.readFirstMeaningfulLine()
        ?: "site.addzero:${sourceDirectory.name}"
    KmpModuleModel(
        gradleFile = gradleFile,
        sourceDirectory = sourceDirectory,
        configDirectory = configDirectory,
        moduleName = moduleName,
        artifactId = sourceDirectory.name,
        description = description,
        platforms = kmpPlatforms(combinedContent, sourceDirectory),
        mainDependencies = mainDependencies.mapValues { (_, dependencies) -> dependencies.distinct() },
        testDependencies = testDependencies.mapValues { (_, dependencies) -> dependencies.distinct() },
        kspProcessors = kspProcessors.mapValues { (_, processors) -> processors.distinct() },
        settingsGeneration = processorSettingsFor(
            relative = gradleFile.relativeTo(root).toString(),
            content = moduleContent,
        ),
        composeEnabled = combinedContent.contains("org.jetbrains.compose") ||
            combinedContent.contains("compose.desktop.currentOs"),
        serializationEnabled = usesKotlinSerializationPlugin(combinedContent),
        koinCompilerEnabled = combinedContent.contains("io.insert-koin.compiler.plugin"),
        compilerPlugins = kmpCompilerPluginsFor(gradleFile.relativeTo(root).toString(), combinedContent),
        jvmRelease = if (moduleContent.contains("jvmToolchain(8)") ||
            moduleContent.contains("JvmTarget.JVM_1_8")
        ) 8 else 17,
        publishingEnabled = !sourceDirectory.name.endsWith("-smoke") &&
            publicationOwnersByArtifact[sourceDirectory.fileName] == sourceDirectory,
    )
}

if (unresolved.isNotEmpty()) {
    error(
        buildString {
            appendLine("Unresolved Gradle dependencies:")
            unresolved.sorted().forEach { dependency -> appendLine("- $dependency") }
        },
    )
}

models.forEach { model ->
    model.configDirectory.createDirectories()
    if (model.configDirectory != model.sourceDirectory) {
        createSourceLink(model.configDirectory, model.sourceDirectory)
    }
    createCandidateMavenSourceLink(model.sourceDirectory)
    model.configDirectory.resolve("module.yaml").writeText(renderModule(model))
}
createFileLink(kmpProjectRoot.resolve("libs.versions.toml"), catalogFile)
val processorSettingsBuildDirectory = root.resolve("build-tools/processor-settings-build")
val kmpProcessorSettingsBuildDirectory = kmpProjectRoot.resolve("build-tools/processor-settings-build")
createFileLink(
    kmpProcessorSettingsBuildDirectory.resolve("module.yaml"),
    processorSettingsBuildDirectory.resolve("module.yaml"),
)
createFileLink(
    kmpProcessorSettingsBuildDirectory.resolve("plugin.yaml"),
    processorSettingsBuildDirectory.resolve("plugin.yaml"),
)
createMergedFileLinks(
    kmpProcessorSettingsBuildDirectory.resolve("src"),
    listOf(processorSettingsBuildDirectory.resolve("src")),
)
kmpModels.forEach { model ->
    model.configDirectory.createDirectories()
    createKmpSourceLinks(model.configDirectory, model.sourceDirectory)
    model.configDirectory.resolve("module.yaml").writeText(renderKmpModule(model))
    deleteTree(overlayRoot.resolve(model.moduleName))
}
supportModules.forEach { supportModule ->
    supportModule.configDirectory.createDirectories()
    if (supportModule.sourceDirectories.none { source -> source.startsWith(supportModule.configDirectory) }) {
        createMavenKotlinSourceLinks(supportModule.configDirectory, supportModule.sourceDirectories)
    }
    supportModule.configDirectory.resolve("module.yaml").writeText(renderSupportModule(supportModule))
}
loggerSampleAppDirectory.resolve("module.yaml").writeText(
    """
    product: jvm/app
    layout: maven-like
    description: "KSP logger processor sample application."

    dependencies:
      - //lib/ksp/logger-api
      - //lib/ksp/logger-implementation

    settings:
      kotlin:
        ksp:
          version: 2.3.9
          processors:
            - //lib/ksp/logger-processor
      jvm:
        mainClass: MainKt
        jdk:
          version: 17
        release: 17
    """.trimIndent() + "\n",
)
listOf(
    "common-models-jvm",
    "gen-reified-core-jvm",
    "tool-jdbc-model-jvm",
    "tool-json-jvm",
    "kcp-multireceiver-annotations-jvm",
    "kcp-transform-overload-annotations-jvm",
).forEach { legacyModule -> deleteTree(overlayRoot.resolve(legacyModule)) }

val auditDirectory = root.resolve("toolchain")
auditDirectory.createDirectories()
auditDirectory.resolve("jvm-lib-modules.txt").writeText(
    models.sortedBy(ModuleModel::moduleName)
        .joinToString(separator = "\n", postfix = "\n") { model ->
            "${model.moduleName}\t${model.sourceDirectory.relativeTo(root)}"
        },
)
auditDirectory.resolve("kmp-lib-modules.txt").writeText(
    kmpModels.sortedBy(KmpModuleModel::moduleName)
        .joinToString(separator = "\n", postfix = "\n") { model ->
            "${model.moduleName}\t${model.sourceDirectory.relativeTo(root)}\t${model.platforms.joinToString(",")}" 
        },
)
auditDirectory.resolve("jvm-publishing-modules.txt").writeText(
    (models.filter(ModuleModel::publishingEnabled).map(ModuleModel::moduleName) +
        supportModules.map(SupportModule::moduleName))
        .sorted()
        .joinToString(separator = "\n", postfix = "\n"),
)
auditDirectory.resolve("kmp-publishing-modules.txt").writeText(
    kmpModels.filter(KmpModuleModel::publishingEnabled)
        .sortedBy(KmpModuleModel::moduleName)
        .joinToString(separator = "\n", postfix = "\n", transform = KmpModuleModel::moduleName),
)
auditDirectory.resolve("jvm-app-modules.txt").writeText(
    "app\t${loggerSampleAppDirectory.relativeTo(root)}\n",
)
auditDirectory.resolve("excluded-modules.txt").writeText(
    exclusions.entries.filterNot { (_, reason) -> reason in setOf("kmp-library", "jvm-app") }
        .sortedBy { entry -> entry.key.toString() }
        .joinToString(separator = "\n", postfix = "\n") { (file, reason) ->
            "${file.parent.relativeTo(root)}\t$reason"
        },
)
auditDirectory.resolve("publishing-disabled-modules.txt").writeText(
    (models.filterNot(ModuleModel::publishingEnabled).map { model ->
        model.moduleName to model.sourceDirectory
    } + kmpModels.filterNot(KmpModuleModel::publishingEnabled).map { model ->
        model.moduleName to model.sourceDirectory
    }).sortedBy(Pair<String, Path>::first)
        .joinToString(separator = "\n", postfix = "\n") { (moduleName, sourceDirectory) ->
            "$moduleName\t${sourceDirectory.relativeTo(root)}"
        },
)

println("Generated ${models.size} JVM library module configurations")
println("Generated ${kmpModels.size} KMP library module configurations")
println("Generated ${supportModules.size} JVM support module configurations")
println(
    "Recorded ${exclusions.count { (_, reason) -> reason !in setOf("kmp-library", "jvm-app") }} unsupported modules",
)

fun exclusionReason(relative: String, content: String): String? = when {
    relative.startsWith("lib/gradle-plugin/") -> "gradle-plugin"
    relative == "lib/ksp/app/build.gradle.kts" -> "jvm-app"
    relative == "lib/ksp/build.gradle.kts" -> "aggregate-project"
    relative == "lib/ksp/metadata/controller2api-idea-plugin/build.gradle.kts" -> "idea-plugin-placeholder"
    relative == "lib/ksp/metadata/kcloud/build.gradle.kts" -> "aggregate-project"
    relative == "lib/ksp/published-gradle-plugin-tests/build.gradle.kts" -> "gradle-plugin-test-suite"
    content.contains("`java-platform`") -> "java-platform"
    content.contains("`java-gradle-plugin`") || Regex("\\bgradlePlugin\\s*\\{").containsMatchIn(content) -> "gradle-plugin"
    content.contains("site.addzero.gradle.plugin.intellij") || content.contains("intellijPlatform") -> "intellij-plugin"
    content.contains("site.addzero.buildlogic.kmp") ||
        content.contains("kotlinMultiplatform") ||
        content.contains("kotlin(\"multiplatform\")") ||
        Regex("id\\(\"kmp-").containsMatchIn(content) -> "kmp-library"
    else -> null
}

fun stripComments(source: String): String {
    val result = StringBuilder(source.length)
    var index = 0
    var blockDepth = 0
    var inLineComment = false
    var inString = false
    var inTripleString = false
    var inCharacter = false
    var escaped = false

    while (index < source.length) {
        val current = source[index]
        val next = source.getOrNull(index + 1)
        val triple = source.substring(index, minOf(index + 3, source.length)) == "\"\"\""

        when {
            inLineComment -> {
                if (current == '\n') {
                    inLineComment = false
                    result.append(current)
                } else {
                    result.append(' ')
                }
            }

            blockDepth > 0 -> {
                when {
                    current == '/' && next == '*' -> {
                        blockDepth += 1
                        result.append("  ")
                        index += 1
                    }

                    current == '*' && next == '/' -> {
                        blockDepth -= 1
                        result.append("  ")
                        index += 1
                    }

                    current == '\n' -> result.append('\n')
                    else -> result.append(' ')
                }
            }

            inTripleString -> {
                result.append(current)
                if (triple) {
                    result.append("\"\"")
                    index += 2
                    inTripleString = false
                }
            }

            inString -> {
                result.append(current)
                if (escaped) {
                    escaped = false
                } else if (current == '\\') {
                    escaped = true
                } else if (current == '"') {
                    inString = false
                }
            }

            inCharacter -> {
                result.append(current)
                if (escaped) {
                    escaped = false
                } else if (current == '\\') {
                    escaped = true
                } else if (current == '\'') {
                    inCharacter = false
                }
            }

            current == '/' && next == '/' -> {
                inLineComment = true
                result.append("  ")
                index += 1
            }

            current == '/' && next == '*' -> {
                blockDepth = 1
                result.append("  ")
                index += 1
            }

            triple -> {
                inTripleString = true
                result.append("\"\"\"")
                index += 2
            }

            current == '"' -> {
                inString = true
                result.append(current)
            }

            current == '\'' -> {
                inCharacter = true
                result.append(current)
            }

            else -> result.append(current)
        }
        index += 1
    }
    return result.toString()
}

fun extractDependencyCalls(content: String): List<Pair<String, String>> {
    val configurations = listOf(
        "testRuntimeOnly",
        "testCompileOnly",
        "testAnnotationProcessor",
        "testImplementation",
        "compileOnlyApi",
        "annotationProcessor",
        "runtimeOnly",
        "compileOnly",
        "implementation",
        "api",
        "kspTest",
        "ksp",
    )
    val pattern = Regex("\\b(${configurations.joinToString("|")})\\s*\\(")
    val calls = mutableListOf<Pair<String, String>>()
    var searchFrom = 0

    while (searchFrom < content.length) {
        val match = pattern.find(content, searchFrom) ?: break
        val openParenthesis = content.indexOf('(', match.range.first)
        val closeParenthesis = findMatchingParenthesis(content, openParenthesis)
        if (closeParenthesis < 0) {
            error("Unclosed dependency call near index ${match.range.first}")
        }
        calls += match.groupValues[1] to content.substring(openParenthesis + 1, closeParenthesis).trim()
        searchFrom = closeParenthesis + 1
    }
    return calls
}

fun extractDependencyCallsWithPositions(content: String): List<DependencyCall> {
    val configurations = listOf(
        "kspCommonMainMetadata",
        "testAnnotationProcessor",
        "testImplementation",
        "testRuntimeOnly",
        "testCompileOnly",
        "compileOnlyApi",
        "annotationProcessor",
        "implementation",
        "runtimeOnly",
        "compileOnly",
        "kspWasmJs",
        "kspAndroid",
        "kspJvm",
        "kspTest",
        "api",
        "ksp",
    )
    val pattern = Regex("\\b(${configurations.joinToString("|")})\\s*\\(")
    val calls = mutableListOf<DependencyCall>()
    var searchFrom = 0
    while (searchFrom < content.length) {
        val match = pattern.find(content, searchFrom) ?: break
        val openParenthesis = content.indexOf('(', match.range.first)
        val closeParenthesis = findMatchingParenthesis(content, openParenthesis)
        require(closeParenthesis >= 0) { "Unclosed dependency call near index ${match.range.first}" }
        calls += DependencyCall(
            configuration = match.groupValues[1],
            expression = content.substring(openParenthesis + 1, closeParenthesis).trim(),
            position = match.range.first,
        )
        searchFrom = closeParenthesis + 1
    }

    val addPattern = Regex("\\badd\\s*\\(\\s*\"([^\"]+)\"\\s*,")
    addPattern.findAll(content).forEach { match ->
        val openParenthesis = content.indexOf('(', match.range.first)
        val closeParenthesis = findMatchingParenthesis(content, openParenthesis)
        require(closeParenthesis >= 0) { "Unclosed add dependency call near index ${match.range.first}" }
        val comma = content.indexOf(',', openParenthesis)
        calls += DependencyCall(
            configuration = match.groupValues[1],
            expression = content.substring(comma + 1, closeParenthesis).trim(),
            position = match.range.first,
        )
    }
    return calls.sortedBy(DependencyCall::position)
}

fun resolveKmpBuildSources(moduleContent: String, conventionFiles: Map<String, Path>): List<String> {
    val resolved = linkedSetOf<String>()
    val sources = mutableListOf<String>()

    fun resolve(content: String) {
        Regex("id\\(\"site\\.addzero\\.buildlogic\\.kmp\\.([^\"]+)\"\\)")
            .findAll(content)
            .map { match -> match.groupValues[1] }
            .forEach { convention ->
                if (!resolved.add(convention)) {
                    return@forEach
                }
                val file = conventionFiles[convention]
                    ?: error("Unknown KMP convention plugin: $convention")
                val conventionContent = stripComments(file.readText())
                resolve(conventionContent)
                sources += conventionContent
            }
    }

    resolve(moduleContent)
    sources += moduleContent
    return sources
}

fun dependencyQualifier(content: String, call: DependencyCall): String {
    if (call.configuration == "kspCommonMainMetadata") {
        return ""
    }
    Regex("^ksp([A-Z][A-Za-z0-9]*?)(?:MainMetadata)?$").matchEntire(call.configuration)?.let { match ->
        return match.groupValues[1].replaceFirstChar(Char::lowercaseChar)
    }
    val sourceSet = enclosingSourceSet(content, call.position) ?: return ""
    return when (sourceSet) {
        "commonMain", "commonTest" -> ""
        "main", "test", "jvmMain", "jvmTest" -> "jvm"
        "webMain", "webTest", "wasmJsMain", "wasmJsTest" -> "wasmJs"
        "nativeMain", "nativeTest" -> "native"
        "iosMain", "iosTest" -> "ios"
        "macosMain", "macosTest" -> "macos"
        "mingwMain", "mingwTest" -> "mingw"
        "linuxMain", "linuxTest" -> "linux"
        else -> sourceSet.removeSuffix("Main").removeSuffix("Test")
    }
}

fun enclosingSourceSet(content: String, position: Int): String? {
    val sourceSetPattern = Regex(
        "\\b(common|main|test|jvm|wasmJs|web|native|android|ios|macos|mingw|linux)" +
            "(Main|Test|UnitTest)?\\s*(?:\\.\\s*dependencies\\s*)?\\{",
    )
    return sourceSetPattern.findAll(content)
        .mapNotNull { match ->
            val openBrace = content.indexOf('{', match.range.first)
            val closeBrace = findMatchingBrace(content, openBrace)
            if (position !in (openBrace + 1) until closeBrace) {
                return@mapNotNull null
            }
            val sourceSet = match.groupValues[1] + match.groupValues[2]
            Triple(sourceSet, openBrace, closeBrace)
        }
        .minByOrNull { (_, openBrace, closeBrace) -> closeBrace - openBrace }
        ?.first
}

fun findMatchingBrace(content: String, openBrace: Int): Int {
    var depth = 0
    var inString = false
    var inTripleString = false
    var inCharacter = false
    var escaped = false
    var index = openBrace
    while (index < content.length) {
        val current = content[index]
        val triple = content.substring(index, minOf(index + 3, content.length)) == "\"\"\""
        when {
            inTripleString -> if (triple) {
                inTripleString = false
                index += 2
            }
            inString -> {
                if (escaped) {
                    escaped = false
                } else if (current == '\\') {
                    escaped = true
                } else if (current == '"') {
                    inString = false
                }
            }
            inCharacter -> {
                if (escaped) {
                    escaped = false
                } else if (current == '\\') {
                    escaped = true
                } else if (current == '\'') {
                    inCharacter = false
                }
            }
            triple -> {
                inTripleString = true
                index += 2
            }
            current == '"' -> inString = true
            current == '\'' -> inCharacter = true
            current == '{' -> depth += 1
            current == '}' -> {
                depth -= 1
                if (depth == 0) {
                    return index
                }
            }
        }
        index += 1
    }
    return -1
}

fun normalizeKmpConfiguration(configuration: String): String = when {
    configuration.startsWith("ksp", ignoreCase = true) -> "ksp"
    configuration.startsWith("test", ignoreCase = true) -> configuration.replaceFirstChar(Char::lowercaseChar)
    else -> configuration
}

fun isTestDependency(content: String, call: DependencyCall): Boolean {
    if (call.configuration.startsWith("test", ignoreCase = true)) {
        return true
    }
    return enclosingSourceSet(content, call.position)?.endsWith("Test") == true
}

fun kmpPlatforms(content: String, sourceDirectory: Path): List<String> {
    val platforms = linkedSetOf<String>()
    val platformPatterns = linkedMapOf(
        "jvm" to Regex("\\bjvm\\s*(?:\\(|\\{)"),
        "android" to Regex("\\bandroid\\s*\\{"),
        "wasmJs" to Regex("\\bwasmJs\\s*\\{"),
        "iosArm64" to Regex("\\biosArm64\\s*\\("),
        "iosSimulatorArm64" to Regex("\\biosSimulatorArm64\\s*\\("),
        "iosX64" to Regex("\\biosX64\\s*\\("),
        "macosArm64" to Regex("\\bmacosArm64\\s*\\("),
        "macosX64" to Regex("\\bmacosX64\\s*\\("),
        "mingwX64" to Regex("\\bmingwX64\\s*\\("),
        "linuxX64" to Regex("\\blinuxX64\\s*\\("),
        "linuxArm64" to Regex("\\blinuxArm64\\s*\\("),
    )
    platformPatterns.forEach { (platform, pattern) ->
        if (pattern.containsMatchIn(content)) {
            platforms += platform
        }
    }
    if (sourceDirectory.endsWith("lib/tool-kmp/network-starter") ||
        sourceDirectory.endsWith("lib/tool-kmp/tool-coll")
    ) {
        platforms += "android"
    }
    if (platforms.isEmpty()) {
        platforms += "jvm"
    }
    return platforms.toList()
}

fun kmpCompilerPluginsFor(relative: String, content: String): List<Pair<String, String>> = buildList {
    val version = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    if (content.contains("site.addzero.kcp.spread-pack")) {
        add("site.addzero.kcp.spread-pack" to "site.addzero:kcp-spread-pack-plugin:$version")
    }
    if (content.contains("de.jensklingenberg.ktorfit")) {
        add(
            "ktorfitPlugin" to
                catalogReference("de-jensklingenberg-ktorfit-ktorfit-compiler-plugin"),
        )
    }
}

fun findMatchingParenthesis(content: String, openParenthesis: Int): Int {
    var depth = 0
    var inString = false
    var inTripleString = false
    var inCharacter = false
    var escaped = false
    var index = openParenthesis

    while (index < content.length) {
        val current = content[index]
        val triple = content.substring(index, minOf(index + 3, content.length)) == "\"\"\""
        when {
            inTripleString -> if (triple) {
                inTripleString = false
                index += 2
            }

            inString -> {
                if (escaped) {
                    escaped = false
                } else if (current == '\\') {
                    escaped = true
                } else if (current == '"') {
                    inString = false
                }
            }

            inCharacter -> {
                if (escaped) {
                    escaped = false
                } else if (current == '\\') {
                    escaped = true
                } else if (current == '\'') {
                    inCharacter = false
                }
            }

            triple -> {
                inTripleString = true
                index += 2
            }

            current == '"' -> inString = true
            current == '\'' -> inCharacter = true
            current == '(' -> depth += 1
            current == ')' -> {
                depth -= 1
                if (depth == 0) {
                    return index
                }
            }
        }
        index += 1
    }
    return -1
}

fun parseDependency(
    expression: String,
    configuration: String,
    root: Path,
    projectAccessorToDirectory: Map<String, Path>,
    gradlePathToDirectory: Map<String, Path>,
    catalogAliasesByModule: Map<String, String>,
    variables: Map<String, String>,
    localModuleNotations: Map<Path, String>,
    localLibraryAliasDirectories: Map<String, Path>,
    projectVersionForNonLocal: String? = null,
): Dependency? {
    val normalized = expression.trim().removeSuffix(",").trim()
    val exported = configuration == "api" || configuration == "compileOnlyApi"
    val scope = when (configuration) {
        "compileOnly", "compileOnlyApi", "testCompileOnly" -> "compile-only"
        "runtimeOnly", "testRuntimeOnly" -> "runtime-only"
        else -> "all"
    }
    val isBom = normalized.contains("platform(")
    val unwrapped = normalized
        .removePrefix("project.dependencies.platform(")
        .removePrefix("enforcedPlatform(")
        .removePrefix("platform(")
        .let { value -> if (isBom && value.endsWith(')')) value.dropLast(1).trim() else value }

    if (unwrapped == "compose.desktop.currentOs") {
        return Dependency("\$compose.desktop.currentOs", exported = exported, scope = scope)
    }

    val projectPath = Regex("project\\(\\s*(?:path\\s*=\\s*)?\"([^\"]+)\"").find(unwrapped)?.groupValues?.get(1)
        ?: Regex("project\\(\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*\\)").find(unwrapped)
            ?.groupValues?.get(1)
            ?.let(variables::get)
    if (projectPath != null) {
        val directory = gradlePathToDirectory[projectPath] ?: return null
        return dependencyForProject(
            directory,
            root,
            catalogAliasesByModule,
            localModuleNotations,
            exported,
            scope,
            projectVersionForNonLocal,
        )
    }

    val accessor = Regex("projects(?:\\.[A-Za-z0-9_]+)+").find(unwrapped)?.value
    if (accessor != null) {
        val directory = projectAccessorToDirectory[accessor]
            ?: error("Unknown Gradle project accessor: $accessor")
        return dependencyForProject(
            directory,
            root,
            catalogAliasesByModule,
            localModuleNotations,
            exported,
            scope,
            projectVersionForNonLocal,
        )
    }

    val alias = Regex("findLibrary\\(\"([^\"]+)\"\\)").find(unwrapped)?.groupValues?.get(1)
        ?: Regex("(?:catalogLibs|libs)\\.([A-Za-z0-9_.]+)").find(unwrapped)?.groupValues?.get(1)
    if (alias != null) {
        val localAlias = alias.replace('.', '-')
        localLibraryAliasDirectories[localAlias]?.let { directory ->
            return dependencyForProject(
                directory,
                root,
                catalogAliasesByModule,
                localModuleNotations,
                exported,
                scope,
                projectVersionForNonLocal,
            )
        }
        return Dependency(catalogReference(alias), exported = exported, scope = scope, bom = isBom)
    }

    val kotlinArtifact = Regex("kotlin\\(\"([^\"]+)\"\\)").find(unwrapped)?.groupValues?.get(1)
    if (kotlinArtifact != null) {
        if (kotlinArtifact == "test" || kotlinArtifact.startsWith("stdlib")) {
            return null
        }
        val aliasName = "org-jetbrains-kotlin-kotlin-$kotlinArtifact"
        return Dependency(catalogReference(aliasName), exported = exported, scope = scope, bom = isBom)
    }

    val rawCoordinate = Regex("^\"([^\"]+)\"").find(unwrapped)?.groupValues?.get(1)
    if (rawCoordinate != null) {
        val resolvedCoordinate = interpolateCoordinate(rawCoordinate, variables)
        val segments = resolvedCoordinate.split(':')
        if (segments.size >= 2) {
            val catalogAlias = catalogAliasesByModule["${segments[0]}:${segments[1]}"]
            if (catalogAlias != null && segments.size <= 3) {
                return Dependency(catalogReference(catalogAlias), exported = exported, scope = scope, bom = isBom)
            }
        }
        if ('$' !in resolvedCoordinate) {
            return Dependency(resolvedCoordinate, exported = exported, scope = scope, bom = isBom)
        }
        return null
    }

    return null
}

fun interpolateCoordinate(coordinate: String, variables: Map<String, String>): String {
    val resolvedVariables = variables + ("javaFxClassifier" to currentJavaFxClassifier())
    return Regex("\\$([A-Za-z_][A-Za-z0-9_]*)").replace(coordinate) { match ->
        resolvedVariables[match.groupValues[1]] ?: match.value
    }
}

fun currentJavaFxClassifier(): String {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    return when {
        osName.contains("mac") && (osArch.contains("aarch64") || osArch.contains("arm64")) -> "mac-aarch64"
        osName.contains("mac") -> "mac"
        osName.contains("win") -> "win"
        osArch.contains("aarch64") || osArch.contains("arm64") -> "linux-aarch64"
        else -> "linux"
    }
}

fun dependencyForProject(
    directory: Path,
    root: Path,
    catalogAliasesByModule: Map<String, String>,
    localModuleNotations: Map<Path, String>,
    exported: Boolean,
    scope: String,
    projectVersionForNonLocal: String?,
): Dependency {
    localModuleNotations[directory]?.let { notation ->
        return Dependency(notation, exported = exported, scope = scope)
    }
    if (directory.name in setOf("tool-modbus", "tool-pinyin")) {
        val alias = catalogAliasesByModule.entries.firstOrNull { (coordinate, _) ->
            coordinate == "site.addzero:${directory.name}"
        }?.value ?: error("Missing catalog alias for site.addzero:${directory.name}")
        return Dependency(catalogReference(alias), exported = exported, scope = scope)
    }
    if (projectVersionForNonLocal != null) {
        return Dependency(
            notation = "site.addzero:${directory.name}:$projectVersionForNonLocal",
            exported = exported,
            scope = scope,
        )
    }

    val alias = catalogAliasesByModule.entries.firstOrNull { (coordinate, _) ->
        coordinate.startsWith("site.addzero:${directory.name}")
    }?.value
    if (alias != null) {
        return Dependency(catalogReference(alias), exported = exported, scope = scope)
    }
    return Dependency(
        notation = "site.addzero:${directory.name}:$publicationVersion",
        exported = exported,
        scope = scope,
    )
}

fun parseCatalogAliasesByModule(content: String): Map<String, String> {
    val result = linkedMapOf<String, String>()
    val libraryLine = Regex("^([A-Za-z0-9_.-]+)\\s*=\\s*\\{(.*)}\\s*$")
    val stringLibraryLine = Regex("^([A-Za-z0-9_.-]+)\\s*=\\s*\"([^\"]+:[^\"]+)\"\\s*$")
    var inLibraries = false
    content.lineSequence().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.startsWith('[')) {
            inLibraries = trimmed == "[libraries]"
            return@forEach
        }
        if (!inLibraries) return@forEach
        stringLibraryLine.matchEntire(trimmed)?.let { match ->
            val alias = match.groupValues[1]
            val coordinate = match.groupValues[2].split(':').take(2).joinToString(":")
            result.putIfAbsent(coordinate, alias)
            return@forEach
        }
        val match = libraryLine.matchEntire(trimmed) ?: return@forEach
        val alias = match.groupValues[1]
        val body = match.groupValues[2]
        val module = Regex("module\\s*=\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
        val group = Regex("group\\s*=\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
        val name = Regex("name\\s*=\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
        val coordinate = module ?: if (group != null && name != null) "$group:$name" else null
        if (coordinate != null) {
            result.putIfAbsent(coordinate, alias)
        }
    }
    return result
}

fun parseStringVariables(content: String): Map<String, String> =
    Regex("\\bval\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*\"([^\"]+)\"")
        .findAll(content)
        .associate { match -> match.groupValues[1] to match.groupValues[2] }

fun String.toBucket(): DependencyBucket = when (this) {
    "testImplementation", "testCompileOnly", "testRuntimeOnly" -> DependencyBucket.Test
    "annotationProcessor" -> DependencyBucket.JavaProcessor
    "testAnnotationProcessor" -> DependencyBucket.JavaTestProcessor
    "ksp" -> DependencyBucket.KspProcessor
    "kspTest" -> DependencyBucket.KspTestProcessor
    else -> DependencyBucket.Main
}

fun isIgnorableDependency(expression: String): Boolean =
    expression.contains("kotlin(\"test\")") ||
        expression.contains("kotlin(\"stdlib") ||
        expression.startsWith("files(")

fun templatesFor(content: String): List<String> = buildList {
    add("//build-config/jvm-lib.module-template.yaml")
    if (!content.contains("site.addzero.buildlogic.yudao.yudao-java-starter")) {
        add("//build-config/jvm8.module-template.yaml")
    }
    when {
        content.contains("site.addzero.buildlogic.jvm.jvm-json-withtool") ->
            add("//build-config/jvm-json-withtool.module-template.yaml")
        content.contains("site.addzero.buildlogic.jvm.jvm-json") ->
            add("//build-config/jvm-json.module-template.yaml")
    }
    if (content.contains("site.addzero.buildlogic.jvm.jvm-koin")) {
        add("//build-config/jvm-koin.module-template.yaml")
    }
    if (content.contains("site.addzero.buildlogic.jvm.jimmer-spring")) {
        add("//build-config/jimmer-spring.module-template.yaml")
    } else if (content.contains("site.addzero.buildlogic.jvm.jimmer")) {
        add("//build-config/jimmer.module-template.yaml")
    }
    if (content.contains("site.addzero.buildlogic.spring.spring-starter") ||
        content.contains("site.addzero.buildlogic.spring.spring-lib-convention")
    ) {
        add("//build-config/spring-starter.module-template.yaml")
    } else if (content.contains("spring-common-convention")) {
        add("//build-config/spring-common.module-template.yaml")
    }
    if (content.contains("site.addzero.buildlogic.yudao.yudao-java-starter")) {
        add("//build-config/yudao-java-starter.module-template.yaml")
    }
    if (content.contains("site.addzero.buildlogic.jvm.jvm-flyway")) {
        add("//build-config/jvm-flyway.module-template.yaml")
    }
    if (content.contains("site.addzero.buildlogic.jvm.jvm-ksp")) {
        add("//build-config/jvm-ksp-processor.module-template.yaml")
    }
}

fun compilerPluginsFor(relative: String, content: String): List<Pair<String, String>> = buildList {
    if (relative == "lib/tool-jvm/tool-ai/build.gradle.kts" &&
        content.contains("site.addzero.kcp.transform-overload")
    ) {
        add(
            "site.addzero.kcp.transform-overload" to
                "site.addzero:kcp-transform-overload-plugin:${LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"))}",
        )
    }
}

fun kspProcessorOptionsFor(relative: String): LinkedHashMap<String, String> = when (relative) {
    "lib/ksp/metadata/jimmer-ddl-compiler/jimmer-ddl-compiler-ksp-smoke/build.gradle.kts" -> linkedMapOf(
        "jimmerDdl.databaseType" to "h2",
        "jimmerDdl.outputFormat" to "flyway",
        "jimmerDdl.outputDir" to "build/generated/jimmer-ddl/main/resources/db/migration",
        "jimmerDdl.version" to "9001",
        "jimmerDdl.description" to "ksp_smoke",
        "jimmerDdl.includeComments" to "false",
    )

    else -> linkedMapOf()
}

fun parseDescription(content: String): String? =
    Regex("\\bdescription\\s*=\\s*\"([^\"]+)\"").find(content)?.groupValues?.get(1)

fun Path.readFirstMeaningfulLine(): String? = runCatching {
    Files.readAllLines(this).asSequence()
        .map(String::trim)
        .firstOrNull { line ->
            line.isNotBlank() &&
                !line.startsWith('#') &&
                !line.startsWith("![") &&
                !line.startsWith("[!") &&
                !line.startsWith("```") &&
                !line.startsWith('<') &&
                !line.startsWith('|') &&
                line != "---"
        }
        ?.take(240)
}.getOrNull()

fun Path.toGradleProjectPath(): String = ":" + relativeTo(repositoryRoot).toString().replace('/', ':')

fun Path.toGradleProjectAccessor(): String = "projects." + relativeTo(repositoryRoot).joinToString(".") { segment ->
    segment.toString().split('-').mapIndexed { index, part ->
        if (index == 0) part else part.replaceFirstChar(Char::uppercase)
    }.joinToString("")
}

fun moduleNameFor(directory: Path, libDirectory: Path, duplicateNames: Set<String>): String {
    if (directory.toString().contains("/lib/tool-jvm/yudao3/") && directory.name in duplicateNames) {
        return "yudao3-${directory.name}"
    }
    if (directory.name !in duplicateNames) {
        return directory.name
    }
    return directory.relativeTo(libDirectory).joinToString("-") { segment ->
        segment.toString().lowercase().replace(Regex("[^a-z0-9-]+"), "-").trim('-')
    }
}

fun catalogReference(alias: String): String = "\$libs." + alias.replace('-', '.')

fun createSourceLink(configDirectory: Path, sourceDirectory: Path) {
    val link = configDirectory.resolve("src")
    if (Files.exists(link)) {
        return
    }
    val target = configDirectory.relativize(sourceDirectory.resolve("src"))
    Files.createSymbolicLink(link, target)
}

fun createFileLink(link: Path, target: Path) {
    link.parent.createDirectories()
    Files.deleteIfExists(link)
    Files.createSymbolicLink(link, link.parent.relativize(target))
}

fun createCandidateMavenSourceLink(moduleDirectory: Path) {
    val commonMain = moduleDirectory.resolve("src/commonMain/kotlin")
    val mainKotlin = moduleDirectory.resolve("src/main/kotlin")
    if (!commonMain.isDirectory() || Files.exists(mainKotlin)) return
    mainKotlin.parent.createDirectories()
    Files.createSymbolicLink(mainKotlin, mainKotlin.parent.relativize(commonMain))
}

fun createMavenKotlinSourceLinks(configDirectory: Path, sourceDirectories: List<Path>) {
    val kotlinDirectory = configDirectory.resolve("src/main/kotlin")
    if (Files.exists(kotlinDirectory)) {
        Files.walk(kotlinDirectory).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach(Files::delete)
        }
    }
    kotlinDirectory.createDirectories()
    sourceDirectories.forEach { sourceDirectory ->
        Files.walk(sourceDirectory).use { paths ->
            paths.filter(Files::isRegularFile).forEach { sourceFile ->
                val link = kotlinDirectory.resolve(sourceFile.relativeTo(sourceDirectory).toString())
                link.parent.createDirectories()
                require(!Files.exists(link)) {
                    "Duplicate JVM support source path: ${link.relativeTo(configDirectory)}"
                }
                Files.createSymbolicLink(link, link.parent.relativize(sourceFile))
            }
        }
    }
}

fun createKmpSourceLinks(configDirectory: Path, sourceDirectory: Path) {
    val sourceSets = linkedMapOf(
        "" to listOf("commonMain"),
        "jvm" to listOf("jvmMain", "main"),
        "android" to listOf("androidMain"),
        "wasmJs" to listOf("webMain", "wasmJsMain"),
        "native" to listOf("nativeMain"),
        "ios" to listOf("iosMain"),
        "iosArm64" to listOf("iosArm64Main"),
        "iosSimulatorArm64" to listOf("iosSimulatorArm64Main"),
        "iosX64" to listOf("iosX64Main"),
        "macos" to listOf("macosMain"),
        "macosArm64" to listOf("macosArm64Main"),
        "macosX64" to listOf("macosX64Main"),
        "mingw" to listOf("mingwMain"),
        "mingwX64" to listOf("mingwX64Main"),
        "linux" to listOf("linuxMain"),
        "linuxX64" to listOf("linuxX64Main"),
        "linuxArm64" to listOf("linuxArm64Main"),
    )
    val testSourceSets = sourceSets.mapValues { (qualifier, names) ->
        if (qualifier.isEmpty()) {
            listOf("commonTest")
        } else {
            names.map { name -> name.removeSuffix("Main") + "Test" } +
                if (qualifier == "android") listOf("androidUnitTest") else emptyList()
        }
    }
    sourceSets.forEach { (qualifier, names) ->
        val suffix = qualifier.takeIf(String::isNotEmpty)?.let { "@$it" }.orEmpty()
        createMergedFileLinks(
            configDirectory.resolve("src$suffix"),
            names.map { name -> sourceDirectory.resolve("src/$name/kotlin") },
        )
        createMergedFileLinks(
            configDirectory.resolve("resources$suffix"),
            names.map { name -> sourceDirectory.resolve("src/$name/resources") },
        )
        createMergedFileLinks(
            configDirectory.resolve("composeResources$suffix"),
            names.map { name -> sourceDirectory.resolve("src/$name/composeResources") },
        )
        createMergedFileLinks(
            configDirectory.resolve("cinterop$suffix"),
            names.map { name -> sourceDirectory.resolve("src/$name/cinterop") },
        )
    }
    testSourceSets.forEach { (qualifier, names) ->
        val suffix = qualifier.takeIf(String::isNotEmpty)?.let { "@$it" }.orEmpty()
        createMergedFileLinks(
            configDirectory.resolve("test$suffix"),
            names.map { name -> sourceDirectory.resolve("src/$name/kotlin") },
        )
        createMergedFileLinks(
            configDirectory.resolve("testResources$suffix"),
            names.map { name -> sourceDirectory.resolve("src/$name/resources") },
        )
        createMergedFileLinks(
            configDirectory.resolve("testComposeResources$suffix"),
            names.map { name -> sourceDirectory.resolve("src/$name/composeResources") },
        )
    }

    val manifest = sourceDirectory.resolve("src/androidMain/AndroidManifest.xml")
    if (manifest.exists()) {
        val manifestLink = configDirectory.resolve("src@android/AndroidManifest.xml")
        manifestLink.parent.createDirectories()
        if (!manifestLink.exists()) {
            Files.createSymbolicLink(manifestLink, manifestLink.parent.relativize(manifest))
        }
    }
}

fun createMergedFileLinks(targetDirectory: Path, sourceDirectories: List<Path>) {
    deleteTree(targetDirectory)
    val existingSources = sourceDirectories.filter(Path::isDirectory)
    if (existingSources.isEmpty()) {
        return
    }
    targetDirectory.createDirectories()
    existingSources.forEach { sourceDirectory ->
        Files.walk(sourceDirectory).use { paths ->
            paths.filter(Files::isRegularFile).forEach { sourceFile ->
                val link = targetDirectory.resolve(sourceFile.relativeTo(sourceDirectory).toString())
                link.parent.createDirectories()
                require(!link.exists()) {
                    "Duplicate KMP source path: ${link.relativeTo(targetDirectory)}"
                }
                Files.createSymbolicLink(link, link.parent.relativize(sourceFile))
            }
        }
    }
}

fun deleteTree(path: Path) {
    if (!path.exists()) {
        return
    }
    Files.walk(path).use { paths ->
        paths.sorted(Comparator.reverseOrder()).forEach(Files::delete)
    }
}

fun renderModule(model: ModuleModel): String = buildString {
    appendLine("product: jvm/lib")
    appendLine("description: ${model.description.yamlString()}")
    appendLine()
    appendLine("apply:")
    model.templates.distinct().forEach { template -> appendLine("  - $template") }

    if (model.mainDependencies.isNotEmpty()) {
        appendLine()
        appendLine("dependencies:")
        model.mainDependencies.forEach { dependency -> appendDependency(dependency, "  ") }
    }
    if (model.testDependencies.isNotEmpty()) {
        appendLine()
        appendLine("test-dependencies:")
        model.testDependencies.forEach { dependency -> appendDependency(dependency, "  ") }
    }

    if (model.settingsGeneration != null) {
        appendLine()
        appendLine("plugins:")
        appendLine("  processor-settings-build:")
        appendLine("    enabled: true")
        appendLine("    packageName: ${model.settingsGeneration.packageName}")
        appendLine("    style: ${model.settingsGeneration.style}")
        appendLine("    settingsClassName: ${model.settingsGeneration.settingsClassName}")
        appendLine("    contextClassName: ${model.settingsGeneration.contextClassName}")
        appendLine("    properties:")
        model.settingsGeneration.properties.forEach { (key, value) ->
            appendLine("      ${key.yamlString()}: ${value.yamlString()}")
        }
    }

    appendLine()
    appendPublishing(model.artifactId, model.description, model.publishingEnabled)

    if (model.javaProcessors.isNotEmpty()) {
        appendLine("  java:")
        appendLine("    annotationProcessing:")
        appendLine("      processors:")
        model.javaProcessors.forEach { processor -> appendLine("        - $processor") }
    }
    if (model.kotlinVersion != null || model.kspProcessors.isNotEmpty() || model.compilerPlugins.isNotEmpty() ||
        usesKotlinSerializationPlugin(model.gradleFile.readText())
    ) {
        appendLine("  kotlin:")
        model.kotlinVersion?.let { version -> appendLine("    version: $version") }
        if (usesKotlinSerializationPlugin(model.gradleFile.readText())) {
            appendLine("    serialization: enabled")
        }
        if (model.compilerPlugins.isNotEmpty()) {
            appendLine("    compilerPlugins:")
            model.compilerPlugins.forEach { (id, dependency) ->
                appendLine("      - id: $id")
                appendLine("        dependency: $dependency")
            }
        }
        if (model.kspProcessors.isNotEmpty()) {
            appendLine("    ksp:")
            appendLine("      version: 2.3.9")
            appendLine("      processors:")
            model.kspProcessors.forEach { processor -> appendLine("        - $processor") }
            if (model.kspProcessorOptions.isNotEmpty()) {
                appendLine("      processorOptions:")
                model.kspProcessorOptions.forEach { (key, value) ->
                    appendLine("        ${key.yamlString()}: ${value.yamlString()}")
                }
            }
        }
    }
    if (model.jdkVersion != 8) {
        appendLine("  jvm:")
        appendLine("    jdk:")
        appendLine("      version: ${model.jdkVersion}")
        appendLine("    release: ${model.jdkVersion}")
    }
    if (model.gradleFile.readText().let { content ->
            content.contains("lombok-convention") || content.contains("org-projectlombok-lombok")
        }
    ) {
        appendLine("  lombok:")
        appendLine("    enabled: true")
        appendLine("    version: 1.18.46")
    }
    if (model.javaTestProcessors.isNotEmpty() || model.kspTestProcessors.isNotEmpty()) {
        appendLine()
        appendLine("test-settings:")
        if (model.javaTestProcessors.isNotEmpty()) {
            appendLine("  java:")
            appendLine("    annotationProcessing:")
            appendLine("      processors:")
            model.javaTestProcessors.forEach { processor -> appendLine("        - $processor") }
        }
        if (model.kspTestProcessors.isNotEmpty()) {
            appendLine("  kotlin:")
            appendLine("    ksp:")
            appendLine("      version: 2.3.9")
            appendLine("      processors:")
            model.kspTestProcessors.forEach { processor -> appendLine("        - $processor") }
        }
    }
}

fun renderKmpModule(model: KmpModuleModel): String = buildString {
    appendLine("product:")
    appendLine("  type: kmp/lib")
    appendLine("  platforms: [${model.platforms.joinToString(", ")}]")
    appendLine("description: ${model.description.yamlString()}")
    appendLine()
    appendLine("repositories:")
    appendLine("  - id: mavenLocal")
    appendLine("    url: mavenLocal")
    appendLine("    resolve: true")
    appendLine("    publish: true")
    appendLine("  - id: huawei-maven-central")
    appendLine("    url: https://mirrors.huaweicloud.com/repository/maven")

    model.mainDependencies.forEach { (qualifier, dependencies) ->
        if (dependencies.isEmpty()) {
            return@forEach
        }
        val suffix = qualifier.takeIf(String::isNotEmpty)?.let { "@$it" }.orEmpty()
        appendLine()
        appendLine("dependencies$suffix:")
        dependencies.forEach { dependency -> appendDependency(dependency, "  ") }
    }
    model.testDependencies.forEach { (qualifier, dependencies) ->
        if (dependencies.isEmpty()) {
            return@forEach
        }
        val suffix = qualifier.takeIf(String::isNotEmpty)?.let { "@$it" }.orEmpty()
        appendLine()
        appendLine("test-dependencies$suffix:")
        dependencies.forEach { dependency -> appendDependency(dependency, "  ") }
    }

    if (model.settingsGeneration != null) {
        appendLine()
        appendLine("plugins:")
        appendLine("  processor-settings-build:")
        appendLine("    enabled: true")
        appendLine("    packageName: ${model.settingsGeneration.packageName}")
        appendLine("    style: ${model.settingsGeneration.style}")
        appendLine("    settingsClassName: ${model.settingsGeneration.settingsClassName}")
        appendLine("    contextClassName: ${model.settingsGeneration.contextClassName}")
        appendLine("    properties:")
        model.settingsGeneration.properties.forEach { (key, value) ->
            appendLine("      ${key.yamlString()}: ${value.yamlString()}")
        }
    }

    appendLine()
    val modulePublicationVersion = if (model.artifactId in setOf("network-starter", "tool-coll")) {
        "2026.08.12"
    } else {
        publicationVersion
    }
    appendPublishing(
        model.artifactId,
        model.description,
        model.publishingEnabled,
        modulePublicationVersion,
    )
    appendLine("  kotlin:")
    appendLine("    version: 2.3.21")
    appendLine("    freeCompilerArgs:")
    appendLine("      - -Xexpect-actual-classes")
    if (model.serializationEnabled) {
        appendLine("    serialization: enabled")
    }
    val commonCompilerPlugins = buildList {
        addAll(model.compilerPlugins)
        if (model.koinCompilerEnabled) {
            add(
                "io.insert-koin.compiler.plugin" to
                    catalogReference("io-insert-koin-koin-compiler-plugin"),
            )
        }
    }
    if (commonCompilerPlugins.isNotEmpty()) {
        appendLine("    compilerPlugins:")
        commonCompilerPlugins.forEach { (id, dependency) ->
            appendLine("      - id: $id")
            appendLine("        dependency: $dependency")
            if (id == "io.insert-koin.compiler.plugin") {
                appendLine("        options:")
                appendLine("          compileSafety: \"false\"")
                appendLine("          debugLogs: \"false\"")
                appendLine("          skipDefaultValues: \"true\"")
                appendLine("          unsafeDslChecks: \"true\"")
                appendLine("          userLogs: \"true\"")
            }
            if (id == "ktorfitPlugin") {
                appendLine("        options:")
                appendLine("          enabled: \"true\"")
                appendLine("          logging: \"false\"")
            }
        }
    }
    model.kspProcessors[""]?.takeIf(List<String>::isNotEmpty)?.let { processors ->
        appendLine("    ksp:")
        appendLine("      version: 2.3.9")
        appendLine("      processors:")
        processors.forEach { processor -> appendLine("        - $processor") }
    }
    if (model.composeEnabled) {
        appendLine("  compose:")
        appendLine("    enabled: true")
    }
    if ("jvm" in model.platforms) {
        appendLine("  jvm:")
        appendLine("    jdk:")
        appendLine("      version: 17")
        appendLine("    release: ${model.jvmRelease}")
    }
    if ("android" in model.platforms) {
        val namespace = model.sourceDirectory.relativeTo(libDirectory).joinToString(".") { segment ->
            segment.toString().lowercase().replace(Regex("[^a-z0-9]+"), "")
        }
        appendLine("  android:")
        appendLine("    namespace: site.addzero.$namespace")
    }

    val qualifiedSettings = model.kspProcessors.filterKeys(String::isNotEmpty).toMutableMap()
    qualifiedSettings.forEach { (qualifier, processors) ->
        appendLine()
        appendLine("settings@$qualifier:")
        appendLine("  kotlin:")
        if (processors.isNotEmpty()) {
            appendLine("    ksp:")
            appendLine("      processors:")
            processors.forEach { processor -> appendLine("        - $processor") }
        }
    }
}

fun usesKotlinSerializationPlugin(content: String): Boolean =
    content.contains("plugin.serialization") || content.contains("plugins.kotlinSerialization")

fun renderSupportModule(model: SupportModule): String = buildString {
    appendLine("product: jvm/lib")
    appendLine("description: ${model.description.yamlString()}")
    appendLine()
    appendLine("apply:")
    model.templates.forEach { template -> appendLine("  - $template") }
    if (model.dependencies.isNotEmpty()) {
        appendLine()
        appendLine("dependencies:")
        model.dependencies.forEach { dependency -> appendDependency(dependency, "  ") }
    }
    appendLine()
    appendPublishing(model.artifactId, model.description, enabled = true)
}

fun StringBuilder.appendPublishing(
    artifactId: String,
    description: String,
    enabled: Boolean,
    version: String = publicationVersion,
) {
    appendLine("settings:")
    appendLine("  publishing:")
    appendLine("    enabled: $enabled")
    appendLine("    group: site.addzero")
    appendLine("    artifactId: $artifactId")
    appendLine("    version: $version")
    appendLine("    publishSources: true")
    appendLine("    signArtifacts: $signArtifacts")
    appendLine("    mavenCentral:")
    appendLine("      enabled: ${enabled && signArtifacts}")
    appendLine("      publishingMode: auto")
    appendLine("    pom:")
    appendLine("      name: $artifactId")
    appendLine("      description: ${description.yamlString()}")
    appendLine("      url: https://github.com/zjarlin/addzero-lib-jvm")
    appendLine("      scm: https://github.com/zjarlin/addzero-lib-jvm.git")
    appendLine("      developers:")
    appendLine("        - id: zjarlin")
    appendLine("          name: zjarlin")
    appendLine("          email: zjarlin@outlook.com")
    appendLine("      licenses:")
    appendLine("        - name: The Apache License, Version 2.0")
    appendLine("          url: https://www.apache.org/licenses/LICENSE-2.0.txt")
}

fun StringBuilder.appendDependency(dependency: Dependency, indent: String) {
    if (dependency.bom) {
        appendLine("${indent}- bom: ${dependency.notation}")
        return
    }
    if (!dependency.exported && dependency.scope == "all") {
        appendLine("${indent}- ${dependency.notation}")
        return
    }
    appendLine("${indent}- ${dependency.notation}:")
    if (dependency.exported) {
        appendLine("${indent}    exported: true")
    }
    if (dependency.scope != "all") {
        appendLine("${indent}    scope: ${dependency.scope}")
    }
}

fun String.yamlString(): String = buildString {
    append('"')
    this@yamlString.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(character)
        }
    }
    append('"')
}

fun kotlinCompilerPluginVersionFor(relative: String): String? = when (relative) {
    "lib/kcp/spread-pack/kcp-spread-pack-plugin/build.gradle.kts" -> "2.3.21"

    else -> null
}

fun processorSettingsFor(relative: String, content: String): SettingsGeneration? =
    processorSettingsOverride(relative) ?: parseProcessorBuddySettings(content)

fun processorSettingsOverride(relative: String): SettingsGeneration? = when (relative) {
    "lib/apt/apt-dict-processor/build.gradle.kts" -> SettingsGeneration(
        packageName = "site.addzero.apt.config.generated",
        style = "holder",
        settingsClassName = "DictProcessorSettings",
        contextClassName = "DictProcessorConfig",
        properties = linkedMapOf(
            "dictAptEnabled" to "false",
            "jdbcDriver" to "com.mysql.cj.jdbc.Driver",
            "jdbcUrl" to "jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&connectTimeout=30000&socketTimeout=30000&autoReconnect=true",
            "jdbcUsername" to "root",
            "jdbcPassword" to "",
            "dictTableName" to "sys_dict_type",
            "dictIdColumn" to "dict_type",
            "dictCodeColumn" to "dict_type",
            "dictNameColumn" to "dict_name",
            "dictItemTableName" to "sys_dict_data",
            "dictItemForeignKeyColumn" to "dict_type",
            "dictItemCodeColumn" to "dict_value",
            "dictItemNameColumn" to "dict_label",
            "enumOutputPackage" to "site.addzero.apt.test.enums",
            "enumOutputDirectory" to "src/test/java",
        ),
    )

    "lib/ksp/metadata/jimmer-low-query/jimmer-low-query-processor/build.gradle.kts" -> SettingsGeneration(
        packageName = "site.addzero.jimmer.lowquery.processor.context.generated",
        properties = linkedMapOf("jimmerLowQuery.generatedPackage" to ""),
    )

    "lib/ksp/metadata/spring2ktor-server-processor/build.gradle.kts" -> SettingsGeneration(
        packageName = "site.addzero.springktor.processor.context.generated",
        properties = linkedMapOf("springKtor.generatedPackage" to ""),
    )

    "lib/ksp/metadata/jimmer-ddl-compiler/jimmer-ddl-compiler-processor/build.gradle.kts" -> SettingsGeneration(
        packageName = "site.addzero.jimmer.ddl.compiler.context.generated",
        properties = linkedMapOf(
            "jimmerDdl.enabled" to "true",
            "jimmerDdl.profiles" to "",
            "jimmerDdl.databaseType" to "postgresql",
            "jimmerDdl.outputFormat" to "flyway",
            "jimmerDdl.outputDir" to "build/generated/jimmer-ddl/main/resources/db/migration",
            "jimmerDdl.version" to "1001",
            "jimmerDdl.description" to "jimmer_auto_ddl_generated",
            "jimmerDdl.includePackages" to "",
            "jimmerDdl.excludePackages" to "",
            "jimmerDdl.includeForeignKeys" to "true",
            "jimmerDdl.includeIndexes" to "true",
            "jimmerDdl.includeComments" to "true",
            "jimmerDdl.includeSequences" to "true",
            "jimmerDdl.includeManyToManyTables" to "true",
        ),
    )

    "lib/ksp/metadata/modbus/modbus-ksp/build.gradle.kts" -> SettingsGeneration(
        packageName = "site.addzero.device.protocol.modbus.ksp.context.generated",
        properties = linkedMapOf(
            "addzero.modbus.transports" to "listOf(\"rtu\")",
            "addzero.modbus.codegen.mode" to "listOf(\"server\")",
            "addzero.modbus.contractPackages" to ",",
            "addzero.modbus.contractPackage" to "",
            "addzero.modbus.metadata.providers" to ",",
            "addzero.modbus.database.driverClass" to "",
            "addzero.modbus.database.jdbcUrl" to "",
            "addzero.modbus.database.username" to "",
            "addzero.modbus.database.password" to "",
            "addzero.modbus.database.query" to "",
            "addzero.modbus.database.jsonColumn" to "",
            "addzero.modbus.apiClientPackageName" to "",
            "addzero.modbus.apiClientOutputDir" to "",
            "addzero.modbus.spring.route.outputDir" to "",
            "addzero.modbus.address.lock.path" to "",
            "addzero.modbus.rtu.default.portPath" to "/dev/ttyUSB0",
            "addzero.modbus.rtu.default.unitId" to "1",
            "addzero.modbus.rtu.default.baudRate" to "9600",
            "addzero.modbus.rtu.default.dataBits" to "8",
            "addzero.modbus.rtu.default.stopBits" to "1",
            "addzero.modbus.rtu.default.parity" to "none",
            "addzero.modbus.rtu.default.timeoutMs" to "1000",
            "addzero.modbus.rtu.default.retries" to "2",
            "addzero.modbus.tcp.default.host" to "127.0.0.1",
            "addzero.modbus.tcp.default.port" to "502",
            "addzero.modbus.tcp.default.unitId" to "1",
            "addzero.modbus.tcp.default.timeoutMs" to "1000",
            "addzero.modbus.tcp.default.retries" to "2",
            "addzero.modbus.mqtt.default.brokerUrl" to "tcp://127.0.0.1:1883",
            "addzero.modbus.mqtt.default.clientId" to "modbus-mqtt-client",
            "addzero.modbus.mqtt.default.requestTopic" to "modbus/request",
            "addzero.modbus.mqtt.default.responseTopic" to "modbus/response",
            "addzero.modbus.mqtt.default.qos" to "1",
            "addzero.modbus.mqtt.default.timeoutMs" to "1000",
            "addzero.modbus.mqtt.default.retries" to "2",
        ),
    )

    "lib/tool-jvm/database/ddlgenerator/build.gradle.kts" -> SettingsGeneration(
        packageName = "site.addzero.ddlgenerator.runtime.config.generated",
        properties = linkedMapOf(
            "jdbcUrl" to "",
            "jdbcUsername" to "",
            "jdbcPassword" to "",
            "autoddlForeignKeys" to "true",
            "autoddlKeys" to "true",
            "autoddlAllowDeleteColumn" to "false",
            "springResourcePath" to "",
            "autoddlExcludeTables" to "flyway_schema_history,vector_store,*_mapping",
            "autoddlExcludeColumns" to "",
        ),
    )

    else -> null
}

fun parseProcessorBuddySettings(content: String): SettingsGeneration? {
    val blockMatch = Regex("\\bprocessorBuddy\\s*\\{").find(content) ?: return null
    val openBrace = content.indexOf('{', blockMatch.range.first)
    val closeBrace = findMatchingBrace(content, openBrace)
    require(closeBrace >= 0) { "Unclosed processorBuddy block" }
    val block = content.substring(openBrace + 1, closeBrace)
    val packageName = Regex("packageName\\.set\\(\\s*\"([^\"]+)\"")
        .find(block)
        ?.groupValues
        ?.get(1)
        ?: return null
    val variables = parseStringVariables(content)
    val properties = linkedMapOf<String, String>()
    val entryPattern = Regex("\"([^\"]+)\"\\s+to\\s+")
    entryPattern.findAll(block).forEach { entry ->
        val expressionStart = entry.range.last + 1
        properties[entry.groupValues[1]] = parseProcessorSettingDefault(
            block = block,
            expressionStart = expressionStart,
            variables = variables,
        )
    }
    require(properties.isNotEmpty()) { "processorBuddy mustMap is empty for $packageName" }
    return SettingsGeneration(
        packageName = if (packageName.endsWith(".generated")) packageName else "$packageName.generated",
        properties = properties,
    )
}

fun parseProcessorSettingDefault(
    block: String,
    expressionStart: Int,
    variables: Map<String, String>,
): String {
    val expression = block.substring(expressionStart).trimStart()
    if (expression.startsWith("\"\"\"")) {
        return expression.substringAfter("\"\"\"").substringBefore("\"\"\"")
    }
    if (expression.startsWith('"')) {
        val value = Regex("^\"((?:\\\\.|[^\"\\\\])*)\"")
            .find(expression)
            ?.groupValues
            ?.get(1)
            ?.replace("\\\\\"", "\"")
            ?.replace("\\\\\\\\", "\\\\")
            .orEmpty()
        return value.takeUnless { '$' in it }.orEmpty()
    }
    val identifier = Regex("^([A-Za-z_][A-Za-z0-9_]*)").find(expression)?.groupValues?.get(1)
    return identifier?.let(variables::get).orEmpty()
}
