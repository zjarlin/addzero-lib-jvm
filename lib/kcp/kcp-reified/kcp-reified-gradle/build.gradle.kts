plugins {
    `kotlin-dsl`
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
}

gradlePlugin {
//    website.set("https://github.com/addzero/kcp-reified")
//    vcsUrl.set("https://github.com/addzero/kcp-reified.git")
    plugins {
        create("reifiedPlugin") {
            id = "site.addzero.kcp.reified"
            displayName = "Reified KCP Gradle Plugin"
            description = "Gradle plugin for the Reified Kotlin Compiler Plugin - automatically generates inline reified wrapper methods"
            tags.set(listOf("kotlin", "compiler-plugin", "reified", "kcp"))
            implementationClass = "site.addzero.kcp.reified.gradle.ReifiedGradlePlugin"
        }
    }
}
