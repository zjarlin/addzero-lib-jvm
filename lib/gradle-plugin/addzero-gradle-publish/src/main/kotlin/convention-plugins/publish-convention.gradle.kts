import java.time.LocalDate

plugins {
    id("com.vanniktech.maven.publish")
}

val projectDescription = "kmp+jimmer全栈脚手架"

val gitUrl = "https://gitee.com/zjarlin/addzero.git"
val gitBaseUrl = gitUrl.removeSuffix(".git")
val vlicensesName = "The Apache License, Version 2.0"
val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
val authName = "zjarlin"
val authEmail = "zjarlin@outlook.com"
val gitHost = gitUrl.substringAfter("://").substringBefore("/")
val gitRepoName = gitUrl.substringAfter("://").substringAfter("/").removeSuffix(".git")

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), project.name, project.version.toString())

    pom {
        name.set(project.name)
        description.set(projectDescription)
        inceptionYear.set(LocalDate.now().year.toString())
        url.set(gitBaseUrl)
        licenses {
            license {
                name.set(vlicensesName)
                url.set(licenseUrl)
                distribution.set(licenseUrl)
            }
        }
        developers {
            developer {
                id.set(authName)
                name.set(authName)
                email.set(authEmail)
            }
        }

        scm {
            connection.set("scm:git:git://$gitHost/$gitRepoName.git")
            developerConnection.set("scm:git:ssh://$gitHost/$gitRepoName.git")
            url.set(gitBaseUrl)
        }
    }
}
