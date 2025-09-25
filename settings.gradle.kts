import org.apache.tools.ant.util.FileUtils.getRelativePath

rootProject.name = "addzero-lib"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

dependencyResolutionManagement {
    repositories {
//        mavenLocal()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

fun findAllProjectDirs(rootDir: File): List<File> {
    val result = mutableListOf<File>()
    if (File(rootDir, "build.gradle.kts").exists()) {
        result.add(rootDir)
    }
    rootDir.listFiles { file: File ->
        file.isDirectory
    }?.forEach { subDir ->
        result.addAll(findAllProjectDirs(subDir))
    }
    return result
}

data class ProjectContext(
    val buildLogics: List<File>,
    val modules: List<File>,
)

fun getProjectContext(): ProjectContext {
    val findAllProjectDirs = findAllProjectDirs(rootDir)
    val allProjectDirs = findAllProjectDirs
        .filter { it.absolutePath != rootDir.absolutePath } // 排除根项目本身
    val (buildProj, modules) =
        allProjectDirs.partition {
            val isBuildLogic = it.name.startsWith("build-logic") || it.name.startsWith("buildLogic")
            isBuildLogic
        }
    return ProjectContext(buildProj, modules)
}


fun autoIncludeModules(rootDir: File, vararg blackModuleName: String) {
    autoIncludeModules(rootDir) {
        val relativePath = getRelativePath(rootDir, it)
        val moduleName = ":${relativePath.replace(File.separator, ":")}"
        val isIncluded = moduleName !in blackModuleName
        // 不再在这里打印跳过信息，避免重复
        isIncluded
    }
}

fun autoIncludeModules(rootDir: File, predicate: (File) -> Boolean = { true }) {
    val projectContext = getProjectContext()

    val buildLogicNames = mutableListOf<String>()
    projectContext.buildLogics.forEach {
        settings.includeBuild(it)
        buildLogicNames.add(it.name)
        // Enhanced debug information
        println("🛠️  [Build Logic #${buildLogicNames.size}] Included build logic: ${it.name}")
    }

    val moduleNames = mutableListOf<String>()
    val skippedModuleNames = mutableListOf<String>()

    projectContext.modules
        .forEach {
            val relativePath = getRelativePath(rootDir, it)
            val moduleName = ":${relativePath.replace(File.separator, ":")}"

            if (predicate(it)) {
                moduleNames.add(moduleName)
                settings.include(moduleName)
                println("📦 [Module #${moduleNames.size}] Included module: $moduleName")
            } else {
                skippedModuleNames.add(moduleName)
                println("⏭️  Skipped module: $moduleName")
            }
        }

    // Print detailed information
    println("\n" + """
        Build Logic (${buildLogicNames.size}):
        ${buildLogicNames.joinToString(", ")}
        
        Skipped Modules (${skippedModuleNames.size}):
        ${if (skippedModuleNames.isEmpty()) "None" else skippedModuleNames.joinToString("\n        ")}
        
        Module Count: ${moduleNames.size}
        Total Count: ${buildLogicNames.size + moduleNames.size}
    """.trimIndent() + "\n")
}

val rootDir = settings.layout.rootDirectory.asFile
autoIncludeModules(rootDir,":lib:tool-jvm:jimmer:addzero-jimmer-ksp-autoddl",
    ":lib:tool-jvm:jimmer:addzero-jimmer-ext-lowquery")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"

}
