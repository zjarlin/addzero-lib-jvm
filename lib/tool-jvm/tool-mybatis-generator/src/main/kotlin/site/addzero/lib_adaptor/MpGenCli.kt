package site.addzero.lib_adaptor

import site.addzero.app.startReplMode

fun main() {
    val commands = listOf(
        MpGenRepl()
    )
    
    commands.startReplMode()
}