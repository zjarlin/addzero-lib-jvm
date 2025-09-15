import BuildSettings.AUTH_NAME
import Disposable.LICENSE_NAME
import Disposable.LICENSE_URL
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
        description.set(BuildSettings.PROJECT_DESCRIPTION)
        inceptionYear.set(LocalDate.now().year.toString())
        url.set(BuildSettings.gitBaseUrl)
        licenses {
            license {
                name.set(LICENSE_NAME)
                url.set(LICENSE_URL)
                distribution.set(LICENSE_URL)
            }
        }
        developers {
            developer {
                id.set(AUTH_NAME)
                name.set(AUTH_NAME)
                email.set(BuildSettings.email)
            }
        }

        scm {
            connection.set("scm:git:git://${BuildSettings.gitHost}/${BuildSettings.gitRepoName}.git")
            developerConnection.set("scm:git:ssh://${BuildSettings.gitHost}/${BuildSettings.gitRepoName}.git")
            url.set(BuildSettings.gitBaseUrl)
        }
    }
}
