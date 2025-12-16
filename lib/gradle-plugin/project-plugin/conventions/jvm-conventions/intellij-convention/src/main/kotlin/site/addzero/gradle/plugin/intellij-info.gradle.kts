package site.addzero.gradle.plugin

import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Plugin information configuration
val pluginName = project.name

// Set project properties for plugin
project.ext.set("pluginId", "site.addzero.$pluginName")
project.ext.set("pluginVersion", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
