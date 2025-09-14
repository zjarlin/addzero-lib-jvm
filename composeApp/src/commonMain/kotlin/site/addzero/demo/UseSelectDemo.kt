//package site.addzero.demo
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import site.addzero.annotation.Route
//import site.addzero.component.dropdown.SelectMode
//import site.addzero.component.text.*
//import site.addzero.hook.UseSelect
//
///**
// * UseSelect Hook演示页面
// */
//@Composable
//@Route("组件示例", "UseSelect Hook", routePath = "/hook/useSelect")
//fun UseSelectDemo() {
//    val scrollState = rememberScrollState()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .verticalScroll(scrollState),
//        verticalArrangement = Arrangement.spacedBy(24.dp)
//    ) {
//        // 标题
//        H1(text = "UseSelect Hook演示")
//
//        // 描述
//        BodyLarge(text = "展示UseSelect Hook的使用方式")
//
//        // 单选模式示例
//        H2(text = "单选模式")
//
//        val singleSelectItems = listOf("选项1", "选项2", "选项3", "选项4", "选项5")
//
//        UseSelect(
//            items = singleSelectItems,
//            getLabelFun = { it },
//            placeholder = "请选择一个选项",
//            selectMode = SelectMode.SINGLE
//        ).Render {
//            BodyMedium(
//                text = "选中的值: ${state.selectedValue ?: "无"}",
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//        }
//
//        // 多选模式示例
//        H2(text = "多选模式")
//
//        val multiSelectItems = listOf("苹果", "香蕉", "橙子", "葡萄", "草莓")
//
//        UseSelect(
//            items = multiSelectItems,
//            getLabelFun = { it },
//            placeholder = "请选择多个选项",
//            selectMode = SelectMode.MULTIPLE
//        ).Render {
//            BodyMedium(
//                text = "选中的值: ${if (state.selectedValues.isEmpty()) "无" else state.selectedValues.joinToString(", ")}",
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//        }
//    }
//}
