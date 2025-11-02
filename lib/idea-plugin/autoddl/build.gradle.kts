import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("site.addzero.buildlogic.intellij.intellij-platform")
}

dependencies {
    implementation(projects.lib.ideaPlugin.ideComponentSettings)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.addzero.autoddl"
        name = "AutoDDL"
    }
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}
