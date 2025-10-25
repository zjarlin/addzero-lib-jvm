package site.addzero.buildlogic

import site.addzero.network.call.maven.util.MavenCentralSearchUtil
import site.addzero.util.VersionUtils

//typealias PjPredicate = (Project) -> Boolean

//interface VersionBuddyExtension {
//    var subProjectVersionApplyPredicate: Property<PjPredicate>
//}

fun firstNotBlank(vararg values: String?): String? {
    return values.firstOrNull { !it.isNullOrBlank() }
}

//val createExtension = createExtension<VersionBuddyExtension>().apply {
//    subProjectVersionApplyPredicate.set { path.startsWith(":lib:") }
//}

val groupId = firstNotBlank(project.group.toString().takeIf { it.isNotBlank() && it != "unspecified" }, "") ?: ""
if (groupId.isBlank()) error("auto version error, you must set group")
val propertyVersion = findProperty("version")?.toString()?.takeIf { it != "unspecified" } ?: ""
val projectVersion = project.version.toString().takeIf { it.isNotBlank() && it != "unspecified" } ?: ""

val mavenVersion = MavenCentralSearchUtil.getLatestVersionByGroupId(groupId)
val finalVersion = run {
    if (propertyVersion.isNotBlank()) {
        println("🔄 VersionBuddy UsePropertyVersion: $propertyVersion ")
        propertyVersion
        return@run
    } else if (projectVersion.isNotBlank()) {
        println("🔄 VersionBuddy  UseProjectVersion: $projectVersion ")
        projectVersion
        return@run
    } else {

        if (mavenVersion.isNullOrBlank()) {
            val defaultVersion = VersionUtils.defaultVersion()
            println("🔄 VersionBuddy  NotFound MavenCentral Version use default version: $defaultVersion ")

            defaultVersion
        } else {
            val nextVersion = VersionUtils.nextVersion(mavenVersion)
            println("🔄 VersionBuddy  Found MavenCentral Version  : $mavenVersion => the nextVersion will be use :  $nextVersion")
            nextVersion
        }

    }

}

subprojects {
    version = finalVersion
}
