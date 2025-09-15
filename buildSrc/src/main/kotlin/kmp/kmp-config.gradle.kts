
plugins {
//    id("org.jetbrains.kotlin.multiplatform")
    id("com.codingfeline.buildkonfig")
}

tasks.named("assemble") {
    dependsOn("generateBuildKonfig")
}
