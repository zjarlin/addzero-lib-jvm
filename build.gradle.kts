import site.addzero.network.call.maven.util.MavenUtil
import site.addzero.util.VersionUtil

//allprojects {
//    version = "0.0.645"
//}


val configuredVersion = findProperty("version") as String?
val projectVersion = project.version.toString().takeIf { it != "unspecified" }
val defaultVersion = configuredVersion ?: projectVersion ?: ""
subprojects {
    if (defaultVersion.isNotBlank()) {
        println("current latest version for ${project.name}: $defaultVersion")
        val nextVersion = VersionUtil.nextVersion(defaultVersion)
        println("the next version will be ${project.name}: $nextVersion")
        version = nextVersion
        return@subprojects
    }

    val group = project.group.toString()
    if (group.isBlank()) {
        error("auto version error, you must set group")
    }

    val latestVersion = MavenUtil.getLatestVersion(group, projectDir.name)
    if (latestVersion.isNullOrBlank()) {
        version = VersionUtil.defaultVersion()
    }
    println("current latest version for ${project.name}: $latestVersion")
    val nextVersion = VersionUtil.nextVersion(latestVersion)
    println("the next version will be ${project.name}: $nextVersion")
    version = nextVersion


}









subprojects {
//    println("aaaaaaaaa$path")
    if (path.startsWith(":lib:")) {
//        println("aaaaaaa$name")
        apply(plugin = "site.addzero.publish-buddy")
    }
}
plugins {
//    id("site.addzero.buildlogic.version-buddy") version "+"
//    id("site.addzero.buildlogic.version-buddy") version "+"
    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
