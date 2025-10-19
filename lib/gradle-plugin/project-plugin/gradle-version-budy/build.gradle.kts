plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
//    id("publish-convention")

}
dependencies {
    implementation(gradleApi())
    implementation(projects.lib.gradlePlugin.gradleScriptCore)
       implementation(projects.lib.toolJvm.networkCall.toolApiMaven)

}
