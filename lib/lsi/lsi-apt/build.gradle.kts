plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {

    api(project(":lib:lsi:lsi-core"))
    implementation("site.addzero:tool-str:2026.02.23")
}
