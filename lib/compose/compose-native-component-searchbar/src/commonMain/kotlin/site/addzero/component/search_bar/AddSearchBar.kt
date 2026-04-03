package site.addzero.component.search_bar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.component.button.AddIconButton

/**
 * 搜索栏组件
 * 支持回车搜索和更友好的提示
 */
@Composable
fun AddSearchBar(
    keyword: String,
    onKeyWordChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请输入关键词搜索...",
    leftSloat: @Composable () -> Unit = {},
    rightSloat: @Composable () -> Unit = {},
    fieldHeight: Dp = 56.dp,
    horizontalSpacing: Dp = 12.dp,
    fieldShape: Shape = RoundedCornerShape(8.dp),
    showRefreshButton: Boolean = true,
) {
    val fieldTextStyle = MaterialTheme.typography.bodyMedium.copy(
        lineHeight = 16.sp,
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
    ) {
        leftSloat()

        OutlinedTextField(
            value = keyword,
            onValueChange = onKeyWordChanged,
            placeholder = {
                Text(
                    text = placeholder,
                    style = fieldTextStyle.copy(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (keyword.isNotEmpty()) {
                    IconButton(
                        onClick = { onKeyWordChanged("") },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "清除",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            singleLine = true,
            textStyle = fieldTextStyle,
            shape = fieldShape,
            modifier = Modifier
                .weight(1f)
                .height(fieldHeight)
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyUp &&
                        keyEvent.key == Key.Enter &&
                        keyword.isNotEmpty()
                    ) {
                        onSearch()
                        true
                    } else {
                        false
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            supportingText = null // 移除底部提示文本以保持高度一致
        )

        if (showRefreshButton) {
            AddIconButton(text = "刷新", imageVector = Icons.Default.Refresh, onClick = onSearch)
        }
        rightSloat()
    }
}
