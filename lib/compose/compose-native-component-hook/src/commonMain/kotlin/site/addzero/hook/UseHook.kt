package site.addzero.hook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier

/**
 * 统一的 Compose Hook 渲染约定。
 *
 * 这一层只负责约束“自身可渲染、离开组合时可释放”的最小能力，
 * 不再要求实现类通过不安全的自引用泛型暴露自身状态。
 */
interface UseHook {

    val modifier: Modifier
        get() = Modifier

    val render: @Composable () -> Unit

    val onDispose
        get() = {}

    /**
     * 渲染 Hook 内容，并在离开组合时触发释放回调。
     */
    @Composable
    fun Render(block: @Composable () -> Unit = {}) {
        DisposableEffect(Unit) {
            onDispose {
                onDispose()
            }
        }

        block()
        render()
    }
}
