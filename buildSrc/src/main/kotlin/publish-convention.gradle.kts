import java.time.LocalDate

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), project.name, project.version.toString())

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
                id.set("zjarlin")
                name.set("zjarlin")
                email.set("zjarlin@outlook.com")
            }
        }

        scm {
            connection.set("scm:git:git://${Vars.gitHost}/${Vars.gitRepoName}.git")
            developerConnection.set("scm:git:ssh://${Vars.gitHost}/${Vars.gitRepoName}.git")
            url.set(Vars.gitBaseUrl)
        }
    }
}
