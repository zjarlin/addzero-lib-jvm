package site.addzero.knowledgegraph.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.knowledgegraph.model.NodeCategory
import site.addzero.knowledgegraph.ui.getCategoryColor

// 定义类别分组
data class CategoryGroup(
    val name: String,
    val categories: List<NodeCategory>
)

val categoryGroups = listOf(
    CategoryGroup(
        name = "Shell",
        categories = listOf(
            NodeCategory.SHELL_VARIABLE,
            NodeCategory.SHELL_FUNCTION,
            NodeCategory.SHELL_ALIAS,
            NodeCategory.SHELL_SOURCE,
            NodeCategory.SHELL_PATH,
            NodeCategory.SHELL_EXPORT,
            NodeCategory.SHELL_COMMAND,
            NodeCategory.SHELL_COMMENT,
            NodeCategory.SHELL_CONDITIONAL,
            NodeCategory.SHELL_LOOP,
            NodeCategory.SHELL_EVAL
        )
    ),
    CategoryGroup(
        name = "Neovim",
        categories = listOf(
            NodeCategory.LUA_KEYMAP,
            NodeCategory.LUA_PLUGIN,
            NodeCategory.LUA_OPTION,
            NodeCategory.LUA_FUNCTION,
            NodeCategory.LUA_REQUIRE,
            NodeCategory.LUA_AUTOCMD,
            NodeCategory.LUA_VARIABLE,
            NodeCategory.LUA_TABLE,
            NodeCategory.LUA_COMMAND,
            NodeCategory.LUA_COMMENT,
            NodeCategory.LUA_HIGHLIGHT
        )
    ),
    CategoryGroup(
        name = "Git",
        categories = listOf(
            NodeCategory.GIT_ALIAS,
            NodeCategory.GIT_SECTION,
            NodeCategory.GIT_INCLUDE,
            NodeCategory.GIT_IGNORE
        )
    ),
    CategoryGroup(
        name = "SSH",
        categories = listOf(
            NodeCategory.SSH_HOST,
            NodeCategory.SSH_OPTION,
            NodeCategory.SSH_MATCH,
            NodeCategory.SSH_INCLUDE,
            NodeCategory.SSH_COMMENT
        )
    )
)

@Composable
fun NodeFilterBar(
    selectedCategories: Set<NodeCategory>,
    searchKeyword: String,
    onCategoryToggle: (NodeCategory) -> Unit,
    onClearAll: () -> Unit,
    onSearchKeywordChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedGroup by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1a1a2e))
            .border(1.dp, Color(0xFF2a2a3e), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // 搜索框
        OutlinedTextField(
            value = searchKeyword,
            onValueChange = onSearchKeywordChange,
            placeholder = { Text("搜索节点...", color = Color.White.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 类别筛选器
        categoryGroups.forEach { group ->
            val isExpanded = expandedGroup == group.name
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        expandedGroup = if (isExpanded) null else group.name
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.categories.forEach { category ->
                        FilterChip(
                            label = category.name.replace("SHELL_", "").replace("LUA_", "").replace("_", " ").trim(),
                            isSelected = category in selectedCategories,
                            color = getCategoryColor(category),
                            onClick = { onCategoryToggle(category) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.3f) else Color.Transparent
    val borderColor = if (isSelected) color else Color(0xFF2a2a3e)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
