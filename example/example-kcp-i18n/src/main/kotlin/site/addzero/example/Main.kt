package site.addzero.example

import site.addzero.util.I8nutil

fun main(args: Array<String>) {
    args.firstOrNull()
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.let(I8nutil::setLocale)
    println(helloMessage())
    println(farewellMessage())
}
