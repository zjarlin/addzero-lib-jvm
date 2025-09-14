package site.addzero.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import site.addzero.assist.api
import site.addzero.generated.api.ApiProvider.sysAiPromptApi
import site.addzero.generated.isomorphic.SysAiPromptIso
import com.mikepenz.markdown.utils.MarkdownLogger.d
import org.koin.android.annotation.KoinViewModel

/**
 * 🤖 AI提示词管理视图模型
 *
 * 负责管理AI提示词的CRUD操作，包括：
 * - 获取常用提示词列表
 * - 创建新的提示词
 * - 更新现有提示词
 * - 删除提示词
 * - 搜索和过滤提示词
 */
@KoinViewModel
class AiPromptViewModel : ViewModel() {
    var prompts by mutableStateOf(emptyList<SysAiPromptIso>())
    init {
        loadPrompts()
    }
    /**
     * 加载所有提示词
     */
    fun loadPrompts() {
        api {
            val response = sysAiPromptApi.getPrompts()
            prompts = response
        }
    }
}
