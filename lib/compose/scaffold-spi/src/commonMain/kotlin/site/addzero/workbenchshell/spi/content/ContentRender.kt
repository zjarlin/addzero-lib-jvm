package site.addzero.workbenchshell.spi.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface ContentRender {
    @Composable
    fun Render(
        modifier: Modifier = Modifier,
    ){
    }
}
