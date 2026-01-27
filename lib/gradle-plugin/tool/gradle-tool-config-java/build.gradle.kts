
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}


dependencies {
    implementation(gradleApi())
}

private fun Project.configJavaToolChain(jdkVersion: String) {
    val toInt = jdkVersion.toInt()
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(toInt))
        }
    }
}

/**
 * 配置Java版本兼容性
 * 统一设置sourceCompatibility和targetCompatibility
 */
private fun Project.configureJavaCompatibility(jdkVersion: String) {
    val toInt = jdkVersion.toInt()
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(toInt)
        targetCompatibility = JavaVersion.toVersion(toInt)
    }
}

fun Project.configureJ8(jdkVersion: String) {
    configureJavaCompatibility(jdkVersion)
    configJavaToolChain(jdkVersion)
}

configureJ8("8")
