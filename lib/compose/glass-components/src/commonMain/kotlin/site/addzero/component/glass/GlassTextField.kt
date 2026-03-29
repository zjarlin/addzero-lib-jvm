package site.addzero.component.glass

import androidx.compose.foundation.layout.heightIn
import com.kyant.shapes.RoundedRectangle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * GlassTextField — 单行玻璃风格输入框
 *
 * 使用 [glassEffect] 渲染半透明玻璃质感的单行文本输入框。
 * 内部使用 [BasicTextField] 实现，支持占位文字。
 *
 * @param value 当前输入值
 * @param onValueChange 输入变化回调
 * @param modifier 外部修饰符
 * @param placeholder 占位提示文字
 * @param enabled 是否启用，默认 true
 * @param singleLine 是否单行，默认 true
 * @param shape 输入框形状，默认 12dp 圆角
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    shape: Shape = RoundedRectangle(12.dp),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { androidx.compose.material3.Text(placeholder) },
        singleLine = singleLine,
        modifier = modifier,
        shape = shape,
    )
}

/**
 * GlassTextArea — 多行玻璃风格文本域
 *
 * 使用 [glassEffect] 渲染半透明玻璃质感的多行文本输入区域。
 * 与 [GlassTextField] 的区别在于支持多行输入，且默认最小高度更大。
 *
 * @param value 当前输入值
 * @param onValueChange 输入变化回调
 * @param modifier 外部修饰符
 * @param placeholder 占位提示文字
 * @param enabled 是否启用，默认 true
 * @param shape 文本域形状，默认 16dp 圆角
 */
@Composable
fun GlassTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    shape: Shape = RoundedRectangle(16.dp),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { androidx.compose.material3.Text(placeholder) },
        singleLine = false,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        shape = shape,
        minLines = 5,
    )
}

/**
 * GlassSearchField — 玻璃风格搜索框
 *
 * 带搜索图标的单行输入框，使用 [glassEffect] 渲染。
 * 支持回车触发搜索回调。
 *
 * @param value 当前输入值
 * @param onValueChange 输入变化回调
 * @param onSearch 搜索回调（回车触发）
 * @param modifier 外部修饰符
 * @param placeholder 占位提示文字
 * @param shape 搜索框形状，默认 24dp 圆角（胶囊形）
 */
@Composable
fun GlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索...",
    shape: Shape = RoundedRectangle(24.dp),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { androidx.compose.material3.Text("🔍 $placeholder") },
        singleLine = true,
        modifier = modifier,
        shape = shape,
    )
}
