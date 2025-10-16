package site.addzero.other

import java.time.LocalDate


val PROJECT_DESCRIPTION = "addzero-kmp-scaffold"
val AUTH_NAME = "zjarlin"
val GIT_URL = "https://gitee.com/zjarlin/addzero.git"


val email: String
    get() = "$AUTH_NAME@outlook.com"
val gitBaseUrl: String
    get() = GIT_URL.removeSuffix(".git")

val gitRepoPath: String
    get() = GIT_URL.substringAfter("://").substringAfter("/")

val gitHost: String
    get() = GIT_URL.substringAfter("://").substringBefore("/")

val gitRepoName: String
    get() = gitRepoPath.removeSuffix(".git")


plugins {
    id("com.vanniktech.maven.publish")
}


// 注意：由于Maven Publish插件与Gradle配置缓存存在兼容性问题，
// 在使用publishToMavenLocal或publishToMavenCentral任务时，
// 请使用--no-configuration-cache参数禁用配置缓存
// 例如: ./gradlew publishToMavenLocal --no-configuration-cache

val pjVersion = project.version.toString()
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), project.name, pjVersion)

    pom {
        name.set(project.name)
        description.set(PROJECT_DESCRIPTION)
        inceptionYear.set(LocalDate.now().year.toString())
        url.set(gitBaseUrl)
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set(AUTH_NAME)
                name.set(AUTH_NAME)
                email.set(email)
            }
        }

        scm {
            connection.set("scm:git:git://${gitHost}/${gitRepoName}.git")
            developerConnection.set("scm:git:ssh://${gitHost}/${gitRepoName}.git")
            url.set(gitBaseUrl)
        }
    }
}
