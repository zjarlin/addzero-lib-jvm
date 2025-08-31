package io.gitee.zjarlin.addzero

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.addzero.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "addzero",
    ) {
        App()
    }
}
