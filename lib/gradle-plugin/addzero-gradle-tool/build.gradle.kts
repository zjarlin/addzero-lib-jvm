plugins {
    `kotlin-dsl`
    `java-gradle-plugin`

}
dependencies {
    implementation(gradleApi())
    implementation(libs.gradlePlugin.buildkonfig)
    implementation(libs.gradlePlugin.buildkonfig.cp)
    implementation(libs.snakeyaml)

//    implementation(libs.gradlePlugin.jetbrainsCompose)
    implementation(libs.gradlePlugin.composeCompiler)

//    compileOnly(libs.gradlePlugin.kotlin)

}




