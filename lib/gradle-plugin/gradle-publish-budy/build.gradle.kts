plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
//    id("publish-convention")

}
dependencies {
gradleApi()
    implementation(libs.gradlePlugin.mavenPublish)
}
gradlePlugin {
    plugins {
        register(project.name) {
            id = "site.addzero.publish-buddy"
            implementationClass = "PublishConventionPlugin"
        }
    }
}
