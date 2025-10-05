plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

dependencies {
    implementation(gradleApi())
//    implementation(projects.lib.gradlePlugin.addzeroGradleTool)
//    implementation(projects.lib.gradlePlugin.addzeroGradleTool)

//    implementation(libs.addzero.gradle.tool)
//   implementation("site.addzero:addzero-gradle-tool:+")
//    implementation(project(":lib:gradle-plugin:addzero-gradle-tool"))
}

gradlePlugin {
    plugins {
        register(project.name) {
            id = "site.addzero.repo-buddy"
            implementationClass = "RepoConfigPlugin"
        }
    }
}
