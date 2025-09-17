package site.addzero.app

import site.addzero.cli.CommandLineInterface
import org.koin.core.context.startKoin
import site.addzero.cli.di.allModules
import site.addzero.cli.i18n.LanguageManager

fun main(args: Array<String>) {
    val language = args.firstOrNull { it.startsWith("--lang=") }?.substringAfter("=") ?: "zh"
    LanguageManager.setLanguage(language)

    val cli = CommandLineInterface()
    cli.run(args.filter { !it.startsWith("--lang=") }.toTypedArray())
}
