//package com.addzero.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.addzero.viewmodel.YmlManagerViewModel
//import androidx.lifecycle.viewmodel.compose.viewModel
//import java.io.File
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//
//@Composable
//fun YmlManagerScreen(viewModel: YmlManagerViewModel = viewModel()) {
//    var ymlContent by remember { mutableStateOf(viewModel.ymlContent) }
//    var predefinedVariables by remember { mutableStateOf(viewModel.predefinedVariables) }
//    var newVariableKey by remember { mutableStateOf("") }
//    var newVariableValue by remember { mutableStateOf("") }
//    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
//
//    // 初始化时刷新文件列表
//    LaunchedEffect(Unit) {
//        viewModel.refreshFileList()
//    }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Text("Compose YML Manager", style = MaterialTheme.typography.h4)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        OutlinedTextField(
//            value = ymlContent,
//            onValueChange = {
//                ymlContent = it
//                viewModel.ymlContent = it
//            },
//            label = { Text("YML Content") },
//            modifier = Modifier.fillMaxWidth().weight(1f),
//            multiline = true
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(modifier = Modifier.fillMaxWidth()) {
//            Button(onClick = {
//                // 导入YML文件
//                // 这里需要实现文件选择逻辑
//            }) {
//                Text("Import YML")
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//            Button(onClick = {
//                // 导出YML文件
//                // 这里需要实现文件保存逻辑
//            }) {
//                Text("Export YML")
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//            Button(onClick = {
//                // 批量导入
//                // 这里需要实现多文件选择逻辑
//            }) {
//                Text("Batch Import")
//            }
//            Spacer(modifier = Modifier.width(8.dp))
//            Button(onClick = {
//                // 批量导出
//                // 这�需要实现选择目标目录逻辑
//            }) {
//                Text("Batch Export")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("YML Files", style = MaterialTheme.typography.h6)
//
//        // 显示YML文件列表
//        LazyColumn(modifier = Modifier.fillMaxWidth().height(100.dp)) {
//            items(viewModel.ymlFiles) { file ->
//                Row(modifier = Modifier.fillMaxWidth()) {
//                    Text(file.name, modifier = Modifier.weight(1f))
//                    Button(onClick = {
//                        viewModel.loadYmlFile(file)
//                        ymlContent = viewModel.ymlContent
//                    }) {
//                        Text("Load")
//                    }
//                    Button(onClick = {
//                        viewModel.deleteYmlFile(file)
//                    }) {
//                        Text("Delete")
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("Predefined Variables", style = MaterialTheme.typography.h6)
//
//        Row(modifier = Modifier.fillMaxWidth()) {
//            OutlinedTextField(
//                value = newVariableKey,
//                onValueChange = { newVariableKey = it },
//                label = { Text("Variable Key") },
//                modifier = Modifier.weight(1f)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            OutlinedTextField(
//                value = newVariableValue,
//                onValueChange = { newVariableValue = it },
//                label = { Text("Variable Value") },
//                modifier = Modifier.weight(1f)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Button(onClick = {
//                if (newVariableKey.isNotEmpty() && newVariableValue.isNotEmpty()) {
//                    viewModel.addVariable(newVariableKey, newVariableValue)
//                    predefinedVariables = viewModel.predefinedVariables
//                    newVariableKey = ""
//                    newVariableValue = ""
//                }
//            }) {
//                Text("Add")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // 显示预定义变量列表
//        LazyColumn(modifier = Modifier.fillMaxWidth().height(100.dp)) {
//            items(predefinedVariables.toList()) { (key, value) ->
//                Row(modifier = Modifier.fillMaxWidth()) {
//                    Text("$key: $value", modifier = Modifier.weight(1f))
//                    Button(onClick = {
//                        viewModel.removeVariable(key)
//                        predefinedVariables = viewModel.predefinedVariables
//                    }) {
//                        Text("Remove")
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            val replacedContent = viewModel.replaceVariables(ymlContent)
//            viewModel.ymlContent = replacedContent
//            ymlContent = replacedContent
//        }) {
//            Text("Replace Variables")
//        }
//    }
//}
