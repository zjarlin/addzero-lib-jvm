package site.addzero.knowledgegraph.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import site.addzero.knowledgegraph.model.NodeCategory
import kotlin.random.Random

// 保留用于UI显示的颜色映射函数
fun getCategoryColor(category: NodeCategory): Color = when (category) {
    NodeCategory.SHELL_VARIABLE, NodeCategory.SHELL_EXPORT -> Color(0xFF4CAF50)
    NodeCategory.SHELL_FUNCTION -> Color(0xFF2196F3)
    NodeCategory.SHELL_ALIAS -> Color(0xFFFF9800)
    NodeCategory.SHELL_SOURCE -> Color(0xFFE91E63)
    NodeCategory.SHELL_PATH -> Color(0xFF9C27B0)
    
    NodeCategory.LUA_KEYMAP -> Color(0xFF00BCD4)
    NodeCategory.LUA_PLUGIN -> Color(0xFFFF5722)
    NodeCategory.LUA_OPTION -> Color(0xFF8BC34A)
    NodeCategory.LUA_FUNCTION -> Color(0xFF3F51B5)
    NodeCategory.LUA_REQUIRE -> Color(0xFFCDDC39)
    NodeCategory.LUA_AUTOCMD -> Color(0xFF3F51B5)
    NodeCategory.LUA_VARIABLE -> Color(0xFF4CAF50)
    NodeCategory.LUA_TABLE -> Color(0xFF9C27B0)
    NodeCategory.LUA_COMMAND -> Color(0xFF795548)
    NodeCategory.LUA_COMMENT -> Color(0xFF607D8B)
    NodeCategory.LUA_HIGHLIGHT -> Color(0xFF009688)
    
    NodeCategory.GIT_ALIAS -> Color(0xFFF44336)
    NodeCategory.GIT_SECTION -> Color(0xFF795548)
    NodeCategory.GIT_INCLUDE -> Color(0xFFFF9800)
    NodeCategory.GIT_IGNORE -> Color(0xFF607D8B)
    
    NodeCategory.SSH_HOST -> Color(0xFF009688)
    NodeCategory.SSH_OPTION -> Color(0xFF4CAF50)
    NodeCategory.SSH_MATCH -> Color(0xFF9C27B0)
    NodeCategory.SSH_INCLUDE -> Color(0xFFFF9800)
    NodeCategory.SSH_COMMENT -> Color(0xFF607D8B)
    
    else -> Color(0xFF607D8B)
}

// 保留随机位置生成函数
fun randomPosition(width: Float, height: Float): Offset =
    Offset(
        Random.nextFloat() * width * 0.6f + width * 0.2f,
        Random.nextFloat() * height * 0.6f + height * 0.2f
    )