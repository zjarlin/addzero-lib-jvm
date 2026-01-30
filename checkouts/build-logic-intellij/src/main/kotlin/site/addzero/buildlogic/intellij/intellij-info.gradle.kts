package site.addzero.buildlogic.intellij

import org.jetbrains.changelog.markdownToHTML
import java.time.LocalDate
import java.time.format.DateTimeFormatter


plugins {
    id("org.jetbrains.intellij.platform.base")
}
val pluginName = project.name
intellijPlatform {
    pluginConfiguration {
        id = "site.addzero.$pluginName"
        name = pluginName
        version = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    }
}

intellijPlatform {
    pluginConfiguration {
        vendor {
            name = "zjarlin"
            email = "zjarlin@outlook.com"
        }
        description = "README.md".md2html()
        changeNotes = "CHANGELOG.md".md2html()
    }

}
fun String.md2html(): String {
    val file = File(projectDir, this)
    if (!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }
    val run = file.readText().run {
        markdownToHTML(this)
    }
    return run
}
