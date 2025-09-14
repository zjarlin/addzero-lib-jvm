plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}



dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("gradleKspConfigPlugin") {
            id = "site.addzero.gradle-ksp-rc"
            implementationClass = "site.addzero.gradle.plugin.GradleKspConfigPlugin"
        }
    }
}
