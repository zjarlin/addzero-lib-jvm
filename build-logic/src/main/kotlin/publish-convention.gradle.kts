import java.time.LocalDate
val group = project.property("group").toString()
val authorName = group.split(".").last()
val email = "$authorName@outlook.com"

plugins {
    id("com.vanniktech.maven.publish")
}
// 使用 Vars 中的配置

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), project.name, project.version.toString())

//    val message = providers.gradleProperty("mavenCentralUsername").get()
//    val message1 = providers.gradleProperty("mavenCentralPassword").get()
//    val message2 = providers.gradleProperty("signingInMemoryKey").get()
//    val message3 = providers.gradleProperty("signingInMemoryKeyId").get()
//    val message4 = providers.gradleProperty("signingInMemoryKeyPassword").get()



    pom {
        name.set(project.name)
        description.set(Vars.projectDescription)
        inceptionYear.set(LocalDate.now().year.toString())
        url.set(Vars.gitBaseUrl)
        licenses {
            license {
                name.set(Vars.licenseName)
                url.set(Vars.licenseUrl)
                distribution.set(Vars.licenseUrl)
            }
        }
        developers {
            developer {
                id.set(authorName)
                name.set(authorName)
                email.set(email)
            }
        }

        scm {
            connection.set("scm:git:git://${Vars.gitHost}/${Vars.gitRepoName}.git")
            developerConnection.set("scm:git:ssh://${Vars.gitHost}/${Vars.gitRepoName}.git")
            url.set(Vars.gitBaseUrl)
        }
    }
}
// 替换之前的配置为以下内容
// 等待任务图完全构建后再添加依赖关系
//gradle.taskGraph.whenReady {
//    tasks.withType<GenerateModuleMetadata>().configureEach {
//        if (project.tasks.findByName("plainJavadocJar") != null) {
//            dependsOn("plainJavadocJar")
//        }
//    }
//}
