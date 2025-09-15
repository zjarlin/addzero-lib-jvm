plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    // 添加ASM库用于字节码分析
    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-tree:9.4")
    
    // 添加测试依赖
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

val pluginName = project.name

gradlePlugin {
    plugins {
        create(pluginName) {
            id = "ksp-buddy"
            implementationClass = "site.addzero.gradle.plugin.kspbuddy.KspBuddyPlugin"
            displayName = "KSP Buddy Plugin"
        }
    }
}