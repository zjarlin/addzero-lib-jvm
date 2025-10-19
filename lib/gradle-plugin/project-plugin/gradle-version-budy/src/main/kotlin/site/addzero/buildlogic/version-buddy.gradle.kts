package site.addzero.buildlogic

import site.addzero.network.call.maven.util.MavenUtil
import site.addzero.util.VersionUtils

// 获取默认版本的逻辑：
// 1. 如果gradle.properties里配置了version属性，就用配置的
// 2. 如果project.version有值，就用project.version的值
// 3. 如果都沒有，就用找一下maven中央仓库的最新版本,然后nextVersion  ; 4. 如果都沒有，就用今天的日期格式版本例如: 2025.01.01

val configuredVersion = findProperty("version").toString().takeIf { it != "unspecified" }
val projectVersion = project.version.toString().takeIf { it != "unspecified" }
val defaultVersion = configuredVersion ?: projectVersion ?: ""



subprojects {
    if (defaultVersion.isNotBlank()) {
        println("current properties version for ${project.name}: $defaultVersion")
        val nextVersion = VersionUtils.nextVersion(defaultVersion)
        println("the properties  next version will be ${project.name}: $nextVersion")

        version = nextVersion
        return@subprojects
    }

    val group = project.group.toString()
    if (group.isBlank()) {
        error("auto version error, you must set group")
        return@subprojects
    }

    val latestVersion = MavenUtil.getLatestVersion(group, project.name)
    println("input $group:${project.name}, the mavenutil out put : $latestVersion")


    if (latestVersion.isNullOrBlank()) {
        val defaultVersion1 = VersionUtils.defaultVersion()
        println("the${group}:${project.name} not found using defult version $defaultVersion1")
        version = defaultVersion1
        return@subprojects
    }

    println("successfully get latest version from maven central ${project.name}: $latestVersion")
    val nextVersion = VersionUtils.nextVersion(latestVersion)
    println("the next version will be ${project.name}: $nextVersion")
    version = nextVersion

}


