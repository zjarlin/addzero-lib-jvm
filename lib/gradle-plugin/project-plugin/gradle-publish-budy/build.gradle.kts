plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
//    id("publish-convention")

}
dependencies {
    implementation(gradleApi())
    implementation(libs.gradlePlugin.mavenPublish)
    implementation(projects.lib.gradlePlugin.gradleScriptCore)
}
