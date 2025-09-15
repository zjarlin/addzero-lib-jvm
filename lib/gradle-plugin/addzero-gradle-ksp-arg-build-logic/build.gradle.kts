plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}


dependencies {
    implementation(gradleApi())
}

val pluginName = project.name

val pid = "${BuildSettings.PACKAGE_NAME}.gradle.plugin.${pluginName.replaceFirstChar { it.uppercase() }}Plugin"
gradlePlugin {
    plugins {
        create(pluginName) {
            id = pid
            implementationClass = pid
            displayName = "${pluginName.replaceFirstChar { it.uppercase() }} Plugin"
        }
    }
}
