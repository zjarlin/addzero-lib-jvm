package site.addzero.autocomplete

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// 通用的自动完成项数据类
data class AutoCompleteItem(
    val id: String,
    val displayText: String,
    val secondaryText: String? = null,
    val icon: (@Composable () -> Unit)? = null,
    val data: Any? = null // 可以存储任意相关的数据
)

// 通用的自动完成字段组件
@Composable
fun AutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    getSuggestions: (String) -> List<AutoCompleteItem>,
    modifier: Modifier = Modifier,
    onItemSelected: (AutoCompleteItem) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    maxSuggestions: Int = 20
) {
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<AutoCompleteItem>>(emptyList()) }

    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(value, isFocused) {
        // 只有当输入字符数大于1时才显示建议
        if (isFocused && value.length > 1) {
            suggestions = getSuggestions(value).take(maxSuggestions)
            showSuggestions = suggestions.isNotEmpty()
        } else {
            showSuggestions = false
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                // 只有当输入字符数大于1时才显示建议
                showSuggestions = it.length > 1
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource,
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4A90E2),
                cursorColor = Color(0xFF4A90E2)
            )
        )

        if (showSuggestions && suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            SuggestionsList(
                suggestions = suggestions,
                onSelect = { item ->
                    onItemSelected(item)
                    showSuggestions = false
                }
            )
        }
    }
}

@Composable
private fun SuggestionsList(
    suggestions: List<AutoCompleteItem>,
    onSelect: (AutoCompleteItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1e1e2e))
            .border(1.dp, Color(0xFF2a2a3e), RoundedCornerShape(8.dp))
    ) {
        LazyColumn(
            modifier = Modifier.padding(4.dp)
        ) {
            items(suggestions) { suggestion ->
                SuggestionRow(
                    item = suggestion,
                    onClick = { onSelect(suggestion) },
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    item: AutoCompleteItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item.icon?.let { icon ->
            Box(modifier = Modifier.size(24.dp)) {
                icon()
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.displayText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            item.secondaryText?.let { secondaryText ->
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
