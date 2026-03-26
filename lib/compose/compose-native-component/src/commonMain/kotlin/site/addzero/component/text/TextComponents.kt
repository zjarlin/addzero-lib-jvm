package site.addzero.component.text

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * H1 标题组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun H1(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign
    )
}

/**
 * H2 标题组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun H2(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign
    )
}

/**
 * H3 标题组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun H3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = textAlign
    )
}

/**
 * H4 标题组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun H4(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        textAlign = textAlign
    )
}

/**
 * BodyLarge 文本组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun BodyLarge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        textAlign = textAlign
    )
}

/**
 * BodyMedium 文本组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun BodyMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        textAlign = textAlign
    )
}

/**
 * BodySmall 文本组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun BodySmall(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = textAlign
    )
}

/**
 * Caption 辅助文本组件
 * @param text 文本内容
 * @param modifier 修饰符
 * @param color 文字颜色
 * @param textAlign 文本对齐方式
 */
@Composable
fun Caption(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        textAlign = textAlign
    )
}

@Composable
fun BlueText(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
        textAlign = TextAlign.Center
    )
}
