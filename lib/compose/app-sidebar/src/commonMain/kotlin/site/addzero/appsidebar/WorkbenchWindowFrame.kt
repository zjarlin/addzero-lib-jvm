package site.addzero.appsidebar

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 工作台窗口框架信息。
 */
@Immutable
data class WorkbenchWindowFrame(
    val immersiveTopBar: Boolean = false,
    val topBarHeight: Dp = 56.dp,
    val leadingInset: Dp = 0.dp,
    val trailingInset: Dp = 0.dp,
)

/**
 * 顶栏装饰器，用于在系统窗口框架外包裹统一视觉。
 */
abstract class WorkbenchTopBarDecorator {
    @Composable
    abstract fun Decorate(
        modifier: Modifier,
        content: @Composable () -> Unit,
    )
}

/**
 * 当前窗口框架信息的组合本地。
 */
val LocalWorkbenchWindowFrame = staticCompositionLocalOf {
    WorkbenchWindowFrame()
}

/**
 * 当前顶栏装饰器的组合本地。
 */
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
