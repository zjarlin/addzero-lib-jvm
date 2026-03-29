package site.addzero.kcp.i18n.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.util.Properties

class I18NGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    private val logger = Logging.getLogger(I18NGradleSubplugin::class.java)

    override fun apply(target: Project) {
        val extension = target.extensions.create("i18n", I18NGradleExtension::class.java)
        addRuntimeDependency(target)
        ensureLocaleTaskPlaceholders(target)
        target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            registerLocaleTasks(
                project = target,
                extension = extension,
                compileTaskName = "compileKotlin",
                sourceSetName = "main",
                targetName = "",
            )
        }
        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            registerLocaleTasks(
                project = target,
                extension = extension,
                compileTaskName = "compileKotlinJvm",
                sourceSetName = "jvmMain",
                targetName = "jvm",
            )
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        if (kotlinCompilation.target.platformType != KotlinPlatformType.jvm) {
            return false
        }
        val project = kotlinCompilation.target.project
        if (shouldDisableCompilerPluginForIdeSync(project)) {
            logger.info(
                "Disabling i18n compiler plugin for IDE sync/import in project ${project.path}",
            )
            return false
        }
        return true
    }

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ) = kotlinCompilation.target.project.provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(I18NGradleExtension::class.java)
        buildList {
            add(
                SubpluginOption(RESOURCE_BASE_PATH_OPTION, extension.resourceBasePath.get()),
            )
            add(
                SubpluginOption(SCAN_SCOPE_OPTION, extension.scanScope.get()),
            )
            val generatedCatalogFile = resolveGeneratedCatalogFile(
                project = project,
                kotlinCompilation = kotlinCompilation,
            )
            if (generatedCatalogFile != null) {
                add(SubpluginOption(GENERATED_CATALOG_FILE_OPTION, generatedCatalogFile))
            }
        }
    }

    override fun getCompilerPluginId(): String {
        return COMPILER_PLUGIN_ID
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            coordinates.groupId,
            COMPILER_ARTIFACT_ID,
            coordinates.version,
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getPluginArtifactForNative(): SubpluginArtifact {
        return getPluginArtifact()
    }

    private fun addRuntimeDependency(project: Project) {
        if (project.extensions.extraProperties.has(RUNTIME_MARKER)) {
            return
        }
        project.extensions.extraProperties.set(RUNTIME_MARKER, true)
        val notation = "${coordinates.groupId}:$RUNTIME_ARTIFACT_ID:${coordinates.version}"
        val configurationNames = listOf(
            "implementation",
            "api",
            "jvmMainImplementation",
        )
        configurationNames.forEach { configurationName ->
            if (project.configurations.findByName(configurationName) != null) {
                project.dependencies.add(configurationName, notation)
            }
        }
    }

    private val coordinates: Coordinates by lazy {
        val properties = Properties()
        javaClass.classLoader
            .getResourceAsStream(PROPERTIES_RESOURCE)
            ?.use(properties::load)
            ?: error("Missing $PROPERTIES_RESOURCE")
        Coordinates(
            groupId = properties.getProperty("groupId"),
            version = properties.getProperty("version"),
        )
    }

    private data class Coordinates(
        val groupId: String,
        val version: String,
    )

    private fun registerLocaleTasks(
        project: Project,
        extension: I18NGradleExtension,
        compileTaskName: String,
        sourceSetName: String,
        targetName: String,
    ) {
        configureCompileTaskForCatalogRebuild(project, compileTaskName)
        if (project.extensions.extraProperties.has(LOCALE_TASKS_MARKER)) {
            return
        }
        project.extensions.extraProperties.set(LOCALE_TASKS_MARKER, true)

        val compileTaskProvider = project.tasks.named(compileTaskName)

        project.tasks.named("syncI18nLocales") { task ->
            task.group = "i18n"
            task.description = "Sync managed locale properties files with the generated i18n catalog."
            task.dependsOn(compileTaskProvider)
            task.doLast {
                val managedLocales = resolveManagedLocales(extension)
                if (managedLocales.isEmpty()) {
                    logger.lifecycle("No managed locales configured for ${project.path}; skipping syncI18nLocales.")
                    return@doLast
                }
                val resourceBasePath = extension.resourceBasePath.get()
                val catalogFile = resolveGeneratedCatalogFile(
                    project = project,
                    targetName = targetName,
                    compilationName = "main",
                )
                    ?.let(::File)
                    ?: throw GradleException("Missing generated i18n catalog location for ${project.path}")
                val catalog = readProperties(catalogFile)
                requireCatalog(catalogFile, catalog)
                val resourcesDir = resolveSourceResourcesDir(project, sourceSetName, resourceBasePath)
                managedLocales.forEach { locale ->
                    val localeFile = File(resourcesDir, "$locale.properties")
                    val existing = if (localeFile.isFile) readProperties(localeFile) else emptyMap()
                    writeLocaleFile(localeFile, catalog, existing)
                }
            }
        }

        val checkTaskProvider = project.tasks.named("checkI18nLocales") { task ->
            task.group = "verification"
            task.description = "Verify that managed locale properties files match the generated i18n catalog."
            task.dependsOn(compileTaskProvider)
            task.doLast {
                val managedLocales = resolveManagedLocales(extension)
                if (managedLocales.isEmpty()) {
                    return@doLast
                }
                val resourceBasePath = extension.resourceBasePath.get()
                val catalogFile = resolveGeneratedCatalogFile(
                    project = project,
                    targetName = targetName,
                    compilationName = "main",
                )
                    ?.let(::File)
                    ?: throw GradleException("Missing generated i18n catalog location for ${project.path}")
                val catalog = readProperties(catalogFile)
                requireCatalog(catalogFile, catalog)
                val resourcesDir = resolveSourceResourcesDir(project, sourceSetName, resourceBasePath)
                val failures = managedLocales.mapNotNull { locale ->
                    val localeFile = File(resourcesDir, "$locale.properties")
                    val translations = if (localeFile.isFile) readProperties(localeFile) else emptyMap()
                    buildCheckFailure(
                        locale = locale,
                        catalogKeys = catalog.keys,
                        translationKeys = translations.keys,
                        file = localeFile,
                    )
                }
                if (failures.isNotEmpty()) {
                    throw GradleException(
                        failures.joinToString(
                            separator = "\n\n",
                            prefix = "kcp-i18n locale verification failed.\n\n",
                            postfix = "\n\nRun ./gradlew syncI18nLocales to repair key drift.",
                        ),
                    )
                }
            }
        }

        project.tasks.matching { task -> task.name == "check" }.configureEach { task ->
            task.dependsOn(checkTaskProvider)
        }
    }

    private fun configureCompileTaskForCatalogRebuild(
        project: Project,
        compileTaskName: String,
    ) {
        if (!shouldForceFullCatalogRebuild(project)) {
            return
        }
        project.tasks.withType(KotlinCompile::class.java).configureEach { task ->
            if (task.name != compileTaskName) {
                return@configureEach
            }
            // The compiler plugin writes a single catalog file. For sync/check/build flows we need
            // a full main-source recompile so incremental compilation does not truncate the catalog
            // to only the recently changed files.
            task.incremental = false
            task.outputs.upToDateWhen { false }
        }
    }

    private fun ensureLocaleTaskPlaceholders(project: Project) {
        if (project.tasks.findByName("syncI18nLocales") == null) {
            project.tasks.register("syncI18nLocales") { task ->
                task.group = "i18n"
                task.description = "Sync managed locale properties files with the generated i18n catalog."
            }
        }
        if (project.tasks.findByName("checkI18nLocales") == null) {
            project.tasks.register("checkI18nLocales") { task ->
                task.group = "verification"
                task.description = "Verify that managed locale properties files match the generated i18n catalog."
            }
        }
    }

    private fun resolveGeneratedCatalogFile(
        project: Project,
        kotlinCompilation: KotlinCompilation<*>,
    ): String? {
        return resolveGeneratedCatalogFile(
            project = project,
            targetName = kotlinCompilation.target.name,
            compilationName = kotlinCompilation.compilationName,
        )
    }

    private fun resolveGeneratedCatalogFile(
        project: Project,
        targetName: String,
        compilationName: String,
    ): String? {
        if (compilationName != "main") {
            return null
        }
        val relativePath = buildString {
            append("generated/kcp-i18n/catalog")
            if (targetName.isNotBlank()) {
                append('/')
                append(targetName)
            }
            append('/')
            append(compilationName)
            append("/catalog.properties")
        }
        val outputFile = project.layout.buildDirectory
            .file(relativePath)
            .get()
            .asFile
        return outputFile.absolutePath
    }

    private fun resolveManagedLocales(extension: I18NGradleExtension): List<String> {
        val configured = extension.managedLocales.orNull
            .orEmpty()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
        if (configured.isNotEmpty()) {
            return configured
        }
        return extension.targetLocale.orNull
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let(::listOf)
            .orEmpty()
    }

    private fun resolveSourceResourcesDir(
        project: Project,
        sourceSetName: String,
        resourceBasePath: String,
    ): File {
        val normalizedBasePath = resourceBasePath.trim().trim('/').ifBlank { "i18n" }
        return project.layout.projectDirectory
            .dir("src/$sourceSetName/resources/$normalizedBasePath")
            .asFile
    }

    private fun requireCatalog(
        catalogFile: File,
        catalog: Map<String, String>,
    ) {
        if (!catalogFile.isFile || catalog.isEmpty()) {
            throw GradleException(
                "Missing generated i18n catalog at ${catalogFile.absolutePath}. " +
                    "Compile the JVM main sources first and ensure they contain translatable strings.",
            )
        }
    }

    private fun buildCheckFailure(
        locale: String,
        catalogKeys: Set<String>,
        translationKeys: Set<String>,
        file: File,
    ): String? {
        val missing = catalogKeys.subtract(translationKeys)
        val extra = translationKeys.subtract(catalogKeys)
        if (missing.isEmpty() && extra.isEmpty()) {
            return null
        }
        return buildString {
            append("Locale `")
            append(locale)
            append("` is out of sync: ")
            append(file.absolutePath)
            append('\n')
            if (missing.isNotEmpty()) {
                append("Missing keys:\n")
                missing.sorted().forEach { key ->
                    append("  - ")
                    append(key)
                    append('\n')
                }
            }
            if (extra.isNotEmpty()) {
                append("Extra keys:\n")
                extra.sorted().forEach { key ->
                    append("  - ")
                    append(key)
                    append('\n')
                }
            }
        }.trimEnd()
    }

    private fun writeLocaleFile(
        localeFile: File,
        catalog: Map<String, String>,
        existingTranslations: Map<String, String>,
    ) {
        localeFile.parentFile?.mkdirs()
        localeFile.writeText(
            buildString {
                append("# Generated by kcp-i18n syncI18nLocales. Edit only values after '='.\n")
                catalog.toSortedMap().forEach { (key, sourceText) ->
                    append("# ")
                    append(sourceText.replace('\n', ' '))
                    append('\n')
                    append(escapeKey(key))
                    append('=')
                    append(escapeValue(existingTranslations[key].orEmpty()))
                    append('\n')
                    append('\n')
                }
            },
            Charsets.UTF_8,
        )
    }

    private fun readProperties(file: File): Map<String, String> {
        val entries = linkedMapOf<String, String>()
        if (!file.isFile) {
            return entries
        }
        file.readLines(Charsets.UTF_8).forEach { rawLine ->
            val trimmedStart = rawLine.trimStart()
            if (trimmedStart.isBlank() || trimmedStart.startsWith("#") || trimmedStart.startsWith("!")) {
                return@forEach
            }
            val separatorIndex = findSeparatorIndex(rawLine)
            val rawKey = if (separatorIndex >= 0) {
                rawLine.substring(0, separatorIndex)
            } else {
                rawLine
            }
            val rawValue = if (separatorIndex >= 0) {
                rawLine.substring(separatorIndex + 1).trimStart()
            } else {
                ""
            }
            entries[decodeEscapes(rawKey)] = decodeEscapes(rawValue)
        }
        return entries
    }

    private fun findSeparatorIndex(line: String): Int {
        for (index in line.indices) {
            val current = line[index]
            if ((current == '=' || current == ':') && !isEscaped(line, index)) {
                return index
            }
        }
        return -1
    }

    private fun isEscaped(text: String, index: Int): Boolean {
        var backslashCount = 0
        var cursor = index - 1
        while (cursor >= 0 && text[cursor] == '\\') {
            backslashCount += 1
            cursor -= 1
        }
        return backslashCount % 2 == 1
    }

    private fun decodeEscapes(text: String): String {
        if ('\\' !in text) {
            return text
        }
        val decoded = StringBuilder(text.length)
        var index = 0
        while (index < text.length) {
            val current = text[index]
            if (current != '\\' || index == text.lastIndex) {
                decoded.append(current)
                index += 1
                continue
            }
            val escaped = text[index + 1]
            when (escaped) {
                't' -> decoded.append('\t')
                'r' -> decoded.append('\r')
                'n' -> decoded.append('\n')
                'f' -> decoded.append('\u000C')
                'u' -> {
                    val unicodeEnd = index + 6
                    if (unicodeEnd <= text.length) {
                        decoded.append(text.substring(index + 2, unicodeEnd).toInt(16).toChar())
                        index += 6
                        continue
                    }
                    decoded.append(escaped)
                }
                else -> decoded.append(escaped)
            }
            index += 2
        }
        return decoded.toString()
    }

    private fun escapeKey(text: String): String {
        return buildString {
            text.forEachIndexed { index, char ->
                when {
                    char == '\\' -> append("\\\\")
                    char == '=' -> append("\\=")
                    char == ':' -> append("\\:")
                    char == '\n' -> append("\\n")
                    char == '\r' -> append("\\r")
                    char == '\t' -> append("\\t")
                    char == '#' && index == 0 -> append("\\#")
                    char == '!' && index == 0 -> append("\\!")
                    else -> append(char)
                }
            }
        }
    }

    private fun escapeValue(text: String): String {
        return buildString {
            text.forEachIndexed { index, char ->
                when {
                    char == '\\' -> append("\\\\")
                    char == '\n' -> append("\\n")
                    char == '\r' -> append("\\r")
                    char == '\t' -> append("\\t")
                    char == ' ' && index == 0 -> append("\\ ")
                    else -> append(char)
                }
            }
        }
    }

    companion object {
        const val GRADLE_PLUGIN_ID: String = "site.addzero.kcp.i18n"
        const val COMPILER_PLUGIN_ID: String = "site.addzero.kcp.i18n"
        const val COMPILER_ARTIFACT_ID: String = "kcp-i18n"
        const val RUNTIME_ARTIFACT_ID: String = "kcp-i18n-runtime"
        const val RESOURCE_BASE_PATH_OPTION: String = "resourceBasePath"
        const val GENERATED_CATALOG_FILE_OPTION: String = "generatedCatalogFile"
        const val SCAN_SCOPE_OPTION: String = "scanScope"

        private const val RUNTIME_MARKER = "site.addzero.kcp.i18n.runtime-added"
        private const val LOCALE_TASKS_MARKER = "site.addzero.kcp.i18n.locale-tasks-added"
        private const val PROPERTIES_RESOURCE = "site/addzero/kcp/i18n/gradle-plugin.properties"
    }
}

internal fun shouldDisableCompilerPluginForIdeSync(project: Project): Boolean {
    return shouldDisableCompilerPluginForIdeSync(
        systemProperties = emptyMap(),
        taskNames = project.gradle.startParameter.taskNames,
    )
}

internal fun shouldDisableCompilerPluginForIdeSync(
    @Suppress("UNUSED_PARAMETER") systemProperties: Map<String, String?>,
    taskNames: Iterable<String>,
): Boolean {
    return taskNames.any { taskName ->
        taskName == "ideaSyncTask" ||
            taskName == "prepareKotlinIdeaImport" ||
            taskName.endsWith("SyncTask")
    }
}

internal fun shouldForceFullCatalogRebuild(project: Project): Boolean {
    return shouldForceFullCatalogRebuild(project.gradle.startParameter.taskNames)
}

internal fun shouldForceFullCatalogRebuild(taskNames: Iterable<String>): Boolean {
    val triggeringTasks = setOf(
        "syncI18nLocales",
        "checkI18nLocales",
        "check",
        "build",
    )
    return taskNames.any { taskName ->
        taskName.substringAfterLast(':') in triggeringTasks
    }
}
