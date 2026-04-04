package site.addzero.knowledgegraph.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.knowledgegraph.model.GraphNode
import site.addzero.knowledgegraph.model.NodeCategory
import site.addzero.knowledgegraph.ui.getCategoryColor
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun CodeEditorDrawer(
    isVisible: Boolean,
    currentNode: GraphNode,
    onClose: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editedContent by remember(currentNode) { mutableStateOf(currentNode.content ?: "") }
    var showCopiedToast by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(modifier = modifier) {
            Column(
                modifier = Modifier
                    .width(500.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1a1a2e))
                    .border(1.dp, Color(0xFF2a2a3e), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .padding(20.dp)
            ) {
                // 头部
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentNode.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        currentNode.filePath?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        currentNode.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = getCategoryColor(currentNode.category).copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 类型标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBadge(currentNode.category)
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFF2a2a3e))

                Spacer(modifier = Modifier.height(16.dp))

                // 代码编辑器
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0d0d1a))
                        .border(1.dp, Color(0xFF2a2a3e), RoundedCornerShape(12.dp))
                ) {
                    Column {
                        // 编辑器头部
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1a1a2e))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getLanguageLabel(currentNode.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                        clipboard.setContents(StringSelection(editedContent), null)
                                        showCopiedToast = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "复制",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // 代码内容
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            SyntaxHighlightedEditor(
                                content = editedContent,
                                onContentChange = { editedContent = it },
                                category = currentNode.category
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { editedContent = currentNode.content ?: "" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text("重置")
                    }
                    
                    Button(
                        onClick = { onSave(editedContent) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A90E2)
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存")
                    }
                }
            }

            // 复制成功提示
            if (showCopiedToast) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Toast(
                        message = "已复制到剪贴板",
                        onDismiss = { showCopiedToast = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: NodeCategory) {
    val color = getCategoryColor(category)
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun getLanguageLabel(category: NodeCategory): String = when {
    category.name.startsWith("SHELL") -> "Shell"
    category.name.startsWith("LUA") -> "Lua"
    category.name.startsWith("GIT") -> "Git Config"
    category.name.startsWith("SSH") -> "SSH Config"
    else -> "Plain Text"
}

@Composable
private fun SyntaxHighlightedEditor(
    content: String,
    onContentChange: (String) -> Unit,
    category: NodeCategory
) {
    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        color = Color.White
    )

    BasicTextField(
        value = content,
        onValueChange = onContentChange,
        textStyle = textStyle,
        modifier = Modifier.fillMaxSize(),
        cursorBrush = SolidColor(Color.White),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize()) {
                // 显示行号
                Row {
                    LineNumbers(content.lines().size)
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                }
            }
        }
    )
}

@Composable
private fun LineNumbers(lineCount: Int) {
    Column(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(Color(0xFF0d0d1a)),
        verticalArrangement = Arrangement.Top
    ) {
        for (i in 1..lineCount) {
            Text(
                text = "$i",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
            )
        }
    }
}

@Composable
private fun Toast(
    message: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }

    Surface(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.8f)
    ) {
        Text(
            text = message,
            color = Color.White,
            modifier = Modifier.padding(12.dp)
        )
    }
}