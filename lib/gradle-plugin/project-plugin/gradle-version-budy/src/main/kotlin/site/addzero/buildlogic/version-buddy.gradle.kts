package site.addzero.buildlogic

import site.addzero.network.call.maven.util.MavenUtil
import site.addzero.util.VersionUtil

// 获取默认版本的逻辑：
// 1. 如果gradle.properties里配置了version属性，就用配置的
// 2. 如果project.version有值，就用project.version的值
// 3. 如果都沒有，就用找一下maven中央仓库的最新版本,然后nextVersion  ; 4. 如果都沒有，就用今天的日期格式版本例如: 2025.01.01

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


