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
            id = "io.gitee.zjarlin.gradle-ksp-rc"
            implementationClass = "com.addzero.gradle.plugin.GradleKspConfigPlugin"
        }
    }
}
