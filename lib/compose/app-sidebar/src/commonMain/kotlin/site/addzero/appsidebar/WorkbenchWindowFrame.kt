package site.addzero.appsidebar

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class WorkbenchWindowFrame(
    val immersiveTopBar: Boolean = false,
    val topBarHeight: Dp = 56.dp,
    val leadingInset: Dp = 0.dp,
    val trailingInset: Dp = 0.dp,
)

abstract class WorkbenchTopBarDecorator {
    @Composable
    abstract fun Decorate(
        modifier: Modifier,
        content: @Composable () -> Unit,
    )
}

val LocalWorkbenchWindowFrame = staticCompositionLocalOf {
    WorkbenchWindowFrame()
}

val LocalWorkbenchTopBarDecorator = staticCompositionLocalOf<WorkbenchTopBarDecorator> {
    object : WorkbenchTopBarDecorator() {
        @Composable
        override fun Decorate(
            modifier: Modifier,
            content: @Composable () -> Unit,
        ) {
            Box(
                modifier = modifier,
            ) {
                content()
            }
        }
    }
}
