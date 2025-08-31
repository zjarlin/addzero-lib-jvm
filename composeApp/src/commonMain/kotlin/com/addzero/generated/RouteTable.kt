package com.addzero.generated//package com.addzero.generated
//
//import androidx.compose.runtime.Composable
//import com.addzero.annotation.Route
//
///**
// * 路由表
// * 请勿手动修改此文件
// */
//object RouteTable {
//    /**
//     * 所有路由映射
//     */
//    val allRoutes = mapOf(
//        RouteKeys.RICH_TEXT_DEMO to  @Composable { com.addzero.demo.RichTextDemo() },
//        RouteKeys.ADD_GENERIC_TABLE_EXAMPLE to  @Composable { com.addzero.demo.AddGenericTableExample() },
//        RouteKeys.U_I_COMPONENTS_DEMO to  @Composable { com.addzero.demo.UIComponentsDemo() },
//        RouteKeys.DATE_INPUT_FIELD_EXAMPLE to  @Composable { com.addzero.demo.date_test.DateInputFieldExample() },
//        RouteKeys.DATE_TIME_PICKER_FIELD to  @Composable { com.addzero.demo.date_test.DateTimePickerField() },
//        RouteKeys.TIME_PICKER_FIELD_TEST to  @Composable { com.addzero.demo.date_test.TimePickerFieldTest() },
//        RouteKeys.DATE_PICKER_FIELD_TEST to  @Composable { com.addzero.demo.date_test.DatePickerFieldTest() },
//        RouteKeys.DATE_RANGE_PICKER_FIELD_TEST to  @Composable { com.addzero.demo.date_test.DateRangePickerFieldTest() },
//        RouteKeys.SYS_ICON_SCREEN to  @Composable { com.addzero.demo.icon.SysIconScreen() },
//        RouteKeys.WE_CHAT_ICON to  @Composable { com.addzero.demo.icon.WeChatIcon() },
//        RouteKeys.DRAG_LIST_DEMO to  @Composable { com.addzero.demo.DragListDemo() },
//        RouteKeys.TEST_PICKER to  @Composable { com.addzero.demo.upload.TestPicker() },
//        RouteKeys.FILE_UPLOAD_DRAWER_DEMO to  @Composable { com.addzero.demo.upload.FileUploadDrawerDemo() },
//        RouteKeys.TEST_DIRECTORY_PICKER to  @Composable { com.addzero.demo.upload.TestDirectoryPicker() },
//        RouteKeys.IMAGE_LOAD to  @Composable { com.addzero.demo.ImageLoad() },
//        RouteKeys.LOGIN_SCREEN to  @Composable { com.addzero.ui.auth.LoginScreen() },
//        RouteKeys.ADD_DROP_DOWN_SELECTOR_TEST to  @Composable { com.addzero.component.dropdown.AddDropDownSelectorTest() },
//        RouteKeys.AUTO_COMPLETE_DEMO to  @Composable { com.addzero.component.autocomplet.AutoCompleteDemo() },
//        RouteKeys.HOME_SCREEN to  @Composable { com.addzero.screens.home.HomeScreen() },
//        RouteKeys.ROLE_LIST_SCREEN to  @Composable { com.addzero.screens.role.RoleListScreen() },
//        RouteKeys.EXCEL_TEMPLATE_SIMPLE_TEST to  @Composable { com.addzero.screens.excel.ExcelTemplateSimpleTest() },
//        RouteKeys.EXCEL_METADATA_EXTRACTION_DEMO to  @Composable { com.addzero.screens.excel.ExcelMetadataExtractionDemo() },
//        RouteKeys.EXCEL_TEMPLATE_TEST_SCREEN to  @Composable { com.addzero.screens.excel.ExcelTemplateTestScreen() },
//        RouteKeys.EXCEL_TEMPLATE_DESIGNER_DEMO to  @Composable { com.addzero.screens.excel.ExcelTemplateDesignerDemo() },
//        RouteKeys.EXCEL_TEMPLATE_DESIGNER_SCREEN to  @Composable { com.addzero.screens.excel.ExcelTemplateDesignerScreen() },
//        RouteKeys.EXCEL_METADATA_TEST_SCREEN to  @Composable { com.addzero.screens.excel.ExcelMetadataTestScreen() },
//        RouteKeys.JSON_DESIGNER_DEMO to  @Composable { com.addzero.screens.json.JsonDesignerDemo() },
//        RouteKeys.LABUBU_CHAT_FEATURES to  @Composable { com.addzero.screens.ai.LabubuChatFeatures() },
//        RouteKeys.CHAT_BACKGROUND_DEMO to  @Composable { com.addzero.screens.ai.ChatBackgroundDemo() },
//        RouteKeys.AI_TIMEOUT_AND_ANIMATION_DEMO to  @Composable { com.addzero.screens.ai.AiTimeoutAndAnimationDemo() },
//        RouteKeys.KEYBOARD_SHORTCUTS_DEMO to  @Composable { com.addzero.screens.ai.KeyboardShortcutsDemo() },
//        RouteKeys.ENTER_KEY_TEST_DEMO to  @Composable { com.addzero.screens.ai.EnterKeyTestDemo() },
//        RouteKeys.LABUBU_CHAT_DEMO to  @Composable { com.addzero.screens.ai.LabubuChatDemo() },
//        RouteKeys.SYS_DEPT_SCREEN to  @Composable { com.addzero.screens.dept.SysDeptScreen() },
//        RouteKeys.TABS_EXAMPLE_SCREEN to  @Composable { com.addzero.demo.TabsExampleScreen() },
//        RouteKeys.DICT_MANAGER_SCREEN to  @Composable { com.addzero.screens.dict.DictManagerScreen() }
//    )
//
//    /**
//     * 所有路由元数据
//     */
//    val allMeta = listOf(
//        Route(value = "组件示例", title = "富文本", routePath = "com.addzero.demo.RichTextDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.RichTextDemo"),
//        Route(value = "组件示例", title = "测试表格", routePath = "com.addzero.demo.AddGenericTableExample", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.AddGenericTableExample"),
//        Route(value = "组件示例", title = "UI组件测试", routePath = "com.addzero.demo.UIComponentsDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.UIComponentsDemo"),
//        Route(value = "组件示例", title = "日期输入框", routePath = "/component/dateInputField", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.date_test.DateInputFieldExample"),
//        Route(value = "组件示例", title = "日期时间选择器", routePath = "/component/dateTimePickerField", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.date_test.DateTimePickerField"),
//        Route(value = "组件示例", title = "时间选择器", routePath = "com.addzero.demo.date_test.TimePickerFieldTest", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.date_test.TimePickerFieldTest"),
//        Route(value = "组件示例", title = "日期选择器", routePath = "com.addzero.demo.date_test.DatePickerFieldTest", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.date_test.DatePickerFieldTest"),
//        Route(value = "组件示例", title = "日期范围选择器", routePath = "com.addzero.demo.date_test.DateRangePickerFieldTest", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.date_test.DateRangePickerFieldTest"),
//        Route(value = "组件示例", title = "M3图标树", routePath = "com.addzero.demo.icon.SysIconScreen", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.icon.SysIconScreen"),
//        Route(value = "组件示例", title = "微信图标", routePath = "/component/wechatIcon", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.icon.WeChatIcon"),
//        Route(value = "组件示例", title = "拖拽列表", routePath = "com.addzero.demo.DragListDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.DragListDemo"),
//        Route(value = "组件示例", title = "文件选择器", routePath = "com.addzero.demo.upload.TestPicker", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.upload.TestPicker"),
//        Route(value = "组件示例", title = "文件上传抽屉", routePath = "com.addzero.demo.upload.FileUploadDrawerDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.upload.FileUploadDrawerDemo"),
//        Route(value = "组件示例", title = "文件夹选择器", routePath = "com.addzero.demo.upload.TestDirectoryPicker", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.upload.TestDirectoryPicker"),
//        Route(value = "组件示例", title = "图片加载", routePath = "com.addzero.demo.ImageLoad", icon = "", order = 0.0, qualifiedName = "com.addzero.demo.ImageLoad"),
//        Route(value = "系统页面", title = "登录页", routePath = "/signFirst", icon = "", order = 0.0, qualifiedName = "com.addzero.ui.auth.LoginScreen"),
//        Route(value = "组件示例", title = "测试性别下拉选择", routePath = "com.addzero.component.dropdown.AddDropDownSelectorTest", icon = "", order = 0.0, qualifiedName = "com.addzero.component.dropdown.AddDropDownSelectorTest"),
//        Route(value = "组件示例", title = "自动完成", routePath = "com.addzero.component.autocomplet.AutoCompleteDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.component.autocomplet.AutoCompleteDemo"),
//        Route(value = "", title = "主页", routePath = "/home", icon = "Home", order = 0.0, qualifiedName = "com.addzero.screens.home.HomeScreen"),
//        Route(value = "系统管理", title = "角色管理", routePath = "/system/role", icon = "Group", order = 0.0, qualifiedName = "com.addzero.screens.role.RoleListScreen"),
//        Route(value = "测试", title = "Excel简单测试", routePath = "com.addzero.screens.excel.ExcelTemplateSimpleTest", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.excel.ExcelTemplateSimpleTest"),
//        Route(value = "界面演示", title = "元数据提取演示", routePath = "com.addzero.screens.excel.ExcelMetadataExtractionDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.excel.ExcelMetadataExtractionDemo"),
//        Route(value = "测试", title = "Excel模板测试", routePath = "com.addzero.screens.excel.ExcelTemplateTestScreen", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.excel.ExcelTemplateTestScreen"),
//        Route(value = "界面演示", title = "Excel模板设计器", routePath = "com.addzero.screens.excel.ExcelTemplateDesignerDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.excel.ExcelTemplateDesignerDemo"),
//        Route(value = "工具", title = "Excel模板设计器", routePath = "com.addzero.screens.excel.ExcelTemplateDesignerScreen", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.excel.ExcelTemplateDesignerScreen"),
//        Route(value = "测试", title = "元数据提取测试", routePath = "com.addzero.screens.excel.ExcelMetadataTestScreen", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.excel.ExcelMetadataTestScreen"),
//        Route(value = "工具", title = "JSON设计器", routePath = "com.addzero.screens.json.JsonDesignerScreen", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.json.JsonDesignerScreen"),
//        Route(value = "界面演示", title = "JSON设计器演示", routePath = "com.addzero.screens.json.JsonDesignerDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.json.JsonDesignerDemo"),
//        Route(value = "界面演示", title = "Labubu聊天新功能", routePath = "com.addzero.screens.ai.LabubuChatFeatures", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.ai.LabubuChatFeatures"),
//        Route(value = "界面演示", title = "聊天背景系统", routePath = "com.addzero.screens.ai.ChatBackgroundDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.ai.ChatBackgroundDemo"),
//        Route(value = "界面演示", title = "AI超时和思考动画", routePath = "com.addzero.screens.ai.AiTimeoutAndAnimationDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.ai.AiTimeoutAndAnimationDemo"),
//        Route(value = "界面演示", title = "键盘快捷键", routePath = "com.addzero.screens.ai.KeyboardShortcutsDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.ai.KeyboardShortcutsDemo"),
//        Route(value = "界面演示", title = "回车发送测试", routePath = "com.addzero.screens.ai.EnterKeyTestDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.ai.EnterKeyTestDemo"),
//        Route(value = "界面演示", title = "Labubu聊天风格", routePath = "com.addzero.screens.ai.LabubuChatDemo", icon = "", order = 0.0, qualifiedName = "com.addzero.screens.ai.LabubuChatDemo"),
//        Route(value = "系统管理", title = "部门管理", routePath = "/system/sysDept", icon = "Business", order = 0.0, qualifiedName = "com.addzero.screens.dept.SysDeptScreen"),
//        Route(value = "组件示例", title = "多标签页组件", routePath = "examples/tabs", icon = "Sharp", order = 3.0, qualifiedName = "com.addzero.demo.TabsExampleScreen"),
//        Route(value = "系统管理", title = "字典管理", routePath = "/dict", icon = "Category", order = 3.0, qualifiedName = "com.addzero.screens.dict.DictManagerScreen")
//    )
//
//    /**
//     * 根据路由键获取对应的Composable函数
//     */
//    operator fun get(routeKey: String): @Composable () -> Unit {
//        return allRoutes[routeKey] ?: throw IllegalArgumentException("Route not found")
//    }
//}
