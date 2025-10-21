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

val finalNextVersionProvider = providers.provider {
    val version = firstNotBlank(
        findProperty("version")?.toString()?.takeIf { it != "unspecified" },
        project.version.toString().takeIf { it.isNotBlank() && it != "unspecified" },
        MavenCentralSearchUtil.getLatestVersionByGroupId(groupId),
        VersionUtils.defaultVersion()
    )

    if (version.isNullOrBlank()) null else {
        val nextVersion = VersionUtils.nextVersion(version)
        println("🔄 VersionBuddy: $version → $nextVersion")
        nextVersion
    }
}
val finalVersion = finalNextVersionProvider.get()

subprojects {
//     val startsWith = path.startsWith(":lib:")
//    val shouldApply = createExtension.subProjectVersionApplyPredicate.get()(this)
//    if (!startsWith) return@subprojects

    version = finalVersion
}
