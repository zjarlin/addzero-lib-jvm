//package site.addzero.viewmodel
//
//import androidx.compose.runtime.*
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.launch
//import java.io.File
//
//class YmlManagerViewModel : ViewModel() {
//    var ymlContent by mutableStateOf("")
//        private set
//
//    var ymlFiles by mutableStateOf<List<File>>(emptyList())
//        private set
//
//    var predefinedVariables by mutableStateOf(mapOf<String, String>())
//        private set
//
//    fun loadYmlFile(file: File) {
//        viewModelScope.launch {
//            ymlContent = file.readText()
//        }
//    }
//
//    fun saveYmlFile(file: File) {
//        viewModelScope.launch {
//            file.writeText(ymlContent)
//        }
//    }
//
//    fun deleteYmlFile(file: File) {
//        viewModelScope.launch {
//            file.delete()
//            refreshFileList()
//        }
//    }
//
//    fun refreshFileList() {
//        // 这里需要根据实际的文件存储位置来实现
//        // 示例：假设YML文件存储在特定目录中
//        val ymlDirectory = File("path/to/yml/files")
//        if (ymlDirectory.exists() && ymlDirectory.isDirectory) {
//            ymlFiles = ymlDirectory.listFiles { file -> file.extension == "yml" }?.toList() ?: emptyList()
//        }
//    }
//
//    fun addVariable(key: String, value: String) {
//        predefinedVariables = predefinedVariables + (key to value)
//    }
//
//    fun removeVariable(key: String) {
//        predefinedVariables = predefinedVariables - key
//    }
//
//    fun replaceVariables(content: String): String {
//        var result = content
//        predefinedVariables.forEach { (key, value) ->
//            result = result.replace("{{$key}}", value)
//        }
//        return result
//    }
//
//    // 批量导入YML文件
//    fun batchImport(files: List<File>) {
//        viewModelScope.launch {
//            // 实现批量导入逻辑
//            // 这里可以将文件列表存储到特定目录
//            files.forEach { file ->
//                val targetFile = File("path/to/yml/files", file.name)
//                file.copyTo(targetFile, overwrite = true)
//            }
//            refreshFileList()
//        }
//    }
//
//    // 批量导出YML文件
//    fun batchExport(targetDirectory: File) {
//        viewModelScope.launch {
//            // 实现批量导出逻辑
//            // 将所有YML文件复制到目标目录
//            ymlFiles.forEach { file ->
//                val targetFile = File(targetDirectory, file.name)
//                file.copyTo(targetFile, overwrite = true)
//            }
//        }
//    }
//}
