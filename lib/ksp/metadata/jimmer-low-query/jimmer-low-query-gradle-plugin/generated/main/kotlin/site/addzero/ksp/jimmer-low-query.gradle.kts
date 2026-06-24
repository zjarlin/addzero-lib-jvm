package site.addzero.ksp

import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import site.addzero.ksp.generated.JimmerLowQueryExtension
import site.addzero.ksp.generated.collectJimmerLowQueryExtensionKspArgs
import java.util.Properties

val GENERATED_SERIALIZED_ARGS_PROPERTY =
    "site.addzero.kspconsumer.site.addzero.ksp.jimmer-low-query.serializedArgs"
val JVM_KSP_GENERATED_DIR = "build/generated/ksp/main/kotlin"
val KMP_COMMON_KSP_GENERATED_DIR = "build/generated/ksp/metadata/commonMain/kotlin"
val KMP_JVM_KSP_GENERATED_DIR = "build/generated/ksp/jvm/jvmMain/kotlin"

data class GeneratedPublishedArtifact(
    val artifactKind: String,
    val localProjectPath: String?,
    val artifactId: String,
)

data class GeneratedPublishedDependency(
    val scope: String,
    val artifactKind: String,
    val localProjectPath: String?,
    val artifactId: String?,
    val notation: String?,
)

val generatedProcessorArtifact =
    GeneratedPublishedArtifact(
        artifactKind = "JVM",
        localProjectPath = ":lib:ksp:metadata:jimmer-low-query:jimmer-low-query-processor",
        artifactId = "jimmer-low-query-processor",
    )

val generatedCompanionDependencies =
    listOf(
        GeneratedPublishedDependency(
            scope = "IMPLEMENTATION",
            artifactKind = "JVM",
            localProjectPath = ":lib:ksp:metadata:jimmer-low-query:jimmer-low-query-annotations",
            artifactId = "jimmer-low-query-annotations",
            notation = null,
        ),
    )

fun loadGeneratedCoordinates(classLoader: ClassLoader): Pair<String, String> {
    val resource =
        classLoader.getResourceAsStream("site/addzero/ksp/jimmer-low-query/gradle-plugin.properties")
            ?: error("缺少坐标资源: site/addzero/ksp/jimmer-low-query/gradle-plugin.properties")
    val properties = Properties()
    resource.use(properties::load)
    val groupId =
        properties.getProperty("groupId")
            ?: error("缺少 groupId: site/addzero/ksp/jimmer-low-query/gradle-plugin.properties")
    val version =
        properties.getProperty("version")
            ?: error("缺少 version: site/addzero/ksp/jimmer-low-query/gradle-plugin.properties")
    return groupId to version
}

fun processorConfigurationName(
    artifactKind: String,
    kotlinMultiplatform: Boolean,
): String =
    if (!kotlinMultiplatform) {
        "ksp"
    } else {
        when (artifactKind.uppercase()) {
            "KMP" -> "kspCommonMainMetadata"
            else -> "kspJvm"
        }
    }

fun companionConfigurationName(
    scope: String,
    artifactKind: String,
    kotlinMultiplatform: Boolean,
): String? {
    val prefix =
        if (!kotlinMultiplatform) {
            ""
        } else {
            when (artifactKind.uppercase()) {
                "KMP" -> "commonMain"
                else -> "jvmMain"
            }
        }
    val suffix =
        when (scope.uppercase()) {
            "IMPLEMENTATION" -> "Implementation"
            "API" -> "Api"
            "COMPILE_ONLY" -> "CompileOnly"
            else -> return null
        }
    return if (prefix.isBlank()) {
        suffix.replaceFirstChar(Char::lowercase)
    } else {
        prefix + suffix
    }
}

fun DependencyHandler.addGeneratedDependency(
    project: Project,
    configurationName: String,
    localProjectPath: String?,
    artifactId: String?,
    notation: String?,
    groupId: String,
    version: String,
) {
    if (project.configurations.findByName(configurationName) == null) {
        return
    }

    when {
        localProjectPath != null && project.rootProject.findProject(localProjectPath) != null -> {
            add(configurationName, project.project(localProjectPath))
        }

        notation != null -> {
            add(configurationName, notation)
        }

        artifactId != null -> {
            add(configurationName, "$groupId:$artifactId:$version")
        }

        else -> {
            error("依赖配置缺少 artifactId/notation/localProjectPath")
        }
    }
}

fun Project.configureGeneratedJvmConsumer() {
    extensions.findByType(KotlinJvmProjectExtension::class.java)
        ?.sourceSets
        ?.findByName("main")
        ?.kotlin
        ?.srcDir(JVM_KSP_GENERATED_DIR)

    tasks.withType(KotlinCompile::class.java).configureEach {
        if (name != "kspKotlin") {
            dependsOn("kspKotlin")
        }
    }

    tasks.configureEach {
        if (name == "sourcesJar") {
            dependsOn("kspKotlin")
        }
    }
}

fun Project.configureGeneratedKmpConsumer() {
    extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kotlin ->
        kotlin.sourceSets.findByName("commonMain")?.kotlin?.srcDir(KMP_COMMON_KSP_GENERATED_DIR)
        kotlin.sourceSets.findByName("jvmMain")?.kotlin?.srcDir(KMP_JVM_KSP_GENERATED_DIR)
    }

    val commonMainTaskName = "kspCommonMainKotlinMetadata"

    fun Task.dependsOnCommonMainIfPresent() {
        if (name == commonMainTaskName) {
            return
        }
        if (tasks.names.contains(commonMainTaskName)) {
            dependsOn(commonMainTaskName)
        }
    }

    tasks.withType(KotlinCompile::class.java).configureEach {
        dependsOnCommonMainIfPresent()
        if (name == "compileKotlinJvm" && tasks.names.contains("kspKotlinJvm")) {
            dependsOn("kspKotlinJvm")
        }
    }
    tasks.withType(Kotlin2JsCompile::class.java).configureEach {
        dependsOnCommonMainIfPresent()
    }
    tasks.withType(KspAATask::class.java).configureEach {
        dependsOnCommonMainIfPresent()
    }
    tasks.configureEach {
        if (name.contains("sourcesJar") || name == "jvmJar") {
            dependsOnCommonMainIfPresent()
        }
        if (name in setOf("jvmJar", "jvmSourcesJar") && tasks.names.contains("kspKotlinJvm")) {
            dependsOn("kspKotlinJvm")
        }
    }
}

// 由 ProcessorBuddy 生成，请勿手改。
val generatedKspResourceClassLoader =
    Thread.currentThread().contextClassLoader ?: javaClass.classLoader

// 由 ProcessorBuddy 生成，请勿手改。
val jimmerLowQueryExtension = extensions.create<JimmerLowQueryExtension>("jimmerLowQuery")

pluginManager.apply("com.google.devtools.ksp")

afterEvaluate {
    val args = collectJimmerLowQueryExtensionKspArgs(jimmerLowQueryExtension)
    extensions.extraProperties.set(GENERATED_SERIALIZED_ARGS_PROPERTY, args)
    if (args.isEmpty()) {
        return@afterEvaluate
    }

    val kspExtension =
        extensions.findByName("ksp") as? KspExtension
            ?: return@afterEvaluate
    args.forEach { (key, value) ->
        kspExtension.arg(key, value)
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    project.configureGeneratedJvmConsumer()
    val (groupId, version) = loadGeneratedCoordinates(generatedKspResourceClassLoader)
    dependencies.addGeneratedDependency(
        project = project,
        configurationName = processorConfigurationName(
            artifactKind = generatedProcessorArtifact.artifactKind,
            kotlinMultiplatform = false,
        ),
        localProjectPath = generatedProcessorArtifact.localProjectPath,
        artifactId = generatedProcessorArtifact.artifactId,
        notation = null,
        groupId = groupId,
        version = version,
    )
    generatedCompanionDependencies.forEach { dependency ->
        val configurationName =
            companionConfigurationName(
                scope = dependency.scope,
                artifactKind = dependency.artifactKind,
                kotlinMultiplatform = false,
            ) ?: return@forEach
        dependencies.addGeneratedDependency(
            project = project,
            configurationName = configurationName,
            localProjectPath = dependency.localProjectPath,
            artifactId = dependency.artifactId,
            notation = dependency.notation,
            groupId = groupId,
            version = version,
        )
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    project.configureGeneratedKmpConsumer()
    val (groupId, version) = loadGeneratedCoordinates(generatedKspResourceClassLoader)
    dependencies.addGeneratedDependency(
        project = project,
        configurationName = processorConfigurationName(
            artifactKind = generatedProcessorArtifact.artifactKind,
            kotlinMultiplatform = true,
        ),
        localProjectPath = generatedProcessorArtifact.localProjectPath,
        artifactId = generatedProcessorArtifact.artifactId,
        notation = null,
        groupId = groupId,
        version = version,
    )
    generatedCompanionDependencies.forEach { dependency ->
        val configurationName =
            companionConfigurationName(
                scope = dependency.scope,
                artifactKind = dependency.artifactKind,
                kotlinMultiplatform = true,
            ) ?: return@forEach
        dependencies.addGeneratedDependency(
            project = project,
            configurationName = configurationName,
            localProjectPath = dependency.localProjectPath,
            artifactId = dependency.artifactId,
            notation = dependency.notation,
            groupId = groupId,
            version = version,
        )
    }
}
