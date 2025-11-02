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





