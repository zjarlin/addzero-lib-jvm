package site.addzero.knowledgegraph.model

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

data class GraphNode(
    val id: String,
    val label: String,
    val category: NodeCategory,
    val filePath: String?,
    val content: String?,
    val description: String? = null,
    var position: Offset = Offset.Zero,
    var velocity: Offset = Offset.Zero,
    var isDragging: Boolean = false
) {
    companion object {
        fun randomPosition(width: Float, height: Float): Offset =
            Offset(
                Random.nextFloat() * width * 0.6f + width * 0.2f,
                Random.nextFloat() * height * 0.6f + height * 0.2f
            )
    }
}

data class GraphEdge(
    val source: String,
    val target: String,
    val label: String? = null,
    val type: EdgeType = EdgeType.SOURCE
)

enum class EdgeType {
    SOURCE,      // source/. 引用
    REQUIRE,     // require 引用
    INCLUDE,     // include 引用
    DEPENDS,     // 依赖关系
    SIMILAR      // 相似关系
}

data class GraphData(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)

// 定义节点分类枚举，支持多种节点类型
enum class NodeCategory(val displayName: String) {
    // Shell相关节点
    SHELL_VARIABLE("Shell变量"),
    SHELL_FUNCTION("Shell函数"),
    SHELL_ALIAS("Shell别名"),
    SHELL_EXPORT("导出变量"),
    SHELL_SOURCE("Source引入"),
    SHELL_PATH("PATH设置"),
    SHELL_COMMAND("命令调用"),
    SHELL_COMMENT("注释"),
    SHELL_CONDITIONAL("条件语句"),
    SHELL_LOOP("循环语句"),
    SHELL_EVAL("Eval表达式"),
    
    // Neovim相关节点
    LUA_KEYMAP("按键映射"),
    LUA_OPTION("Vim选项"),
    LUA_AUTOCMD("自动命令"),
    LUA_PLUGIN("插件配置"),
    LUA_FUNCTION("Lua函数"),
    LUA_REQUIRE("模块引入"),
    LUA_VARIABLE("Lua变量"),
    LUA_TABLE("Lua表"),
    LUA_COMMAND("Vim命令"),
    LUA_COMMENT("注释"),
    LUA_HIGHLIGHT("高亮配置"),
    
    // Git相关节点
    GIT_SECTION("Git配置段"),
    GIT_ALIAS("Git别名"),
    GIT_INCLUDE("Git引入"),
    GIT_IGNORE("忽略规则"),
    
    // SSH相关节点
    SSH_HOST("SSH主机"),
    SSH_OPTION("SSH选项"),
    SSH_MATCH("SSH匹配块"),
    SSH_INCLUDE("SSH引入"),
    SSH_COMMENT("注释"),
    
    // 默认节点
    DEFAULT("默认节点");
    
    companion object {
        fun fromString(value: String): NodeCategory = 
            values().find { it.name.equals(value, ignoreCase = true) } ?: DEFAULT
    }
}
