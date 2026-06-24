package site.addzero.ksp

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.create
import site.addzero.ksp.generated.JimmerDdlCompilerExtension
import site.addzero.ksp.generated.collectJimmerDdlCompilerExtensionKspArgs
import java.util.Properties

val COORDINATES_RESOURCE_PATH = "site/addzero/ksp/jimmer-ddl-compiler/gradle-plugin.properties"
val PROCESSOR_LOCAL_PROJECT_PATH = ":lib:ksp:metadata:jimmer-ddl-compiler:jimmer-ddl-compiler-processor"
val PROCESSOR_ARTIFACT_ID = "jimmer-ddl-compiler-processor"
val GENERATED_SERIALIZED_ARGS_PROPERTY = "site.addzero.kspconsumer.site.addzero.ksp.jimmer-ddl-compiler.serializedArgs"

val jimmerDdl = extensions.create<JimmerDdlCompilerExtension>("jimmerDdl")
val pluginClassLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader

fun loadJimmerDdlCompilerCoordinates(): Pair<String, String> {
    val resource = pluginClassLoader.getResourceAsStream(COORDINATES_RESOURCE_PATH)
        ?: error("缺少坐标资源: $COORDINATES_RESOURCE_PATH")
    val properties = Properties()
    resource.use(properties::load)
    val groupId = properties.getProperty("groupId") ?: error("缺少 groupId: $COORDINATES_RESOURCE_PATH")
    val version = properties.getProperty("version") ?: error("缺少 version: $COORDINATES_RESOURCE_PATH")
    return groupId to version
}

fun DependencyHandler.addJimmerDdlCompilerProcessor(
    project: Project,
    configurationName: String,
) {
    if (project.configurations.findByName(configurationName) == null) {
        return
    }
    val localProject = project.rootProject.findProject(PROCESSOR_LOCAL_PROJECT_PATH)
    if (localProject != null) {
        add(configurationName, project.project(PROCESSOR_LOCAL_PROJECT_PATH))
        return
    }
    val (groupId, version) = loadJimmerDdlCompilerCoordinates()
    add(configurationName, "$groupId:$PROCESSOR_ARTIFACT_ID:$version")
}

fun Project.defaultJimmerDdlOutputDir(): String {
    return layout.buildDirectory
        .dir("generated/jimmer-ddl/main/resources/db/migration")
        .get()
        .asFile
        .absolutePath
}

fun Project.collectJimmerDdlArgs(): LinkedHashMap<String, String> {
    return collectJimmerDdlCompilerExtensionKspArgs(
        extension = jimmerDdl,
        defaultOutputDir = defaultJimmerDdlOutputDir(),
    )
}

fun Project.configureJimmerDdlGeneratedResourceDirectory() {
    extensions.findByType(SourceSetContainer::class.java)
        ?.named("main")
        ?.configure {
            resources.srcDir(layout.buildDirectory.dir("generated/jimmer-ddl/main/resources"))
        }
}

fun Project.configureJimmerDdlKspArgs() {
    val kspExtension = extensions.findByName("ksp") as? KspExtension ?: return
    val args = collectJimmerDdlArgs()
    extensions.extraProperties.set(GENERATED_SERIALIZED_ARGS_PROPERTY, args)
    args.forEach { (key, value) ->
        kspExtension.arg(key, value)
    }
}

fun Project.configureJimmerDdlJavaCompilerArgs() {
    val args = collectJimmerDdlArgs()
    tasks.withType(JavaCompile::class.java).configureEach {
        options.compilerArgs.addAll(args.map { (key, value) -> "-A$key=$value" })
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    pluginManager.apply("com.google.devtools.ksp")
    dependencies.addJimmerDdlCompilerProcessor(project, "ksp")
    configureJimmerDdlGeneratedResourceDirectory()
    afterEvaluate {
        configureJimmerDdlKspArgs()
        tasks.named("processResources").configure {
            if (tasks.names.contains("kspKotlin")) {
                dependsOn("kspKotlin")
            }
        }
    }
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    pluginManager.apply("com.google.devtools.ksp")
    dependencies.addJimmerDdlCompilerProcessor(project, "kspJvm")
    configureJimmerDdlGeneratedResourceDirectory()
    afterEvaluate {
        configureJimmerDdlKspArgs()
    }
}

pluginManager.withPlugin("java") {
    dependencies.addJimmerDdlCompilerProcessor(project, "annotationProcessor")
    configureJimmerDdlGeneratedResourceDirectory()
    afterEvaluate {
        configureJimmerDdlJavaCompilerArgs()
        tasks.named("processResources").configure {
            tasks.withType(JavaCompile::class.java).forEach { javaCompile ->
                dependsOn(javaCompile)
            }
        }
    }
}
