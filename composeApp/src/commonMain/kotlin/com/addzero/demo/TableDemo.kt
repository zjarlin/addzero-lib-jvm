@file:OptIn(ExperimentalUuidApi::class)

package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.addzero.annotation.Route
import com.addzero.component.button.AddIconButton
import com.addzero.component.search_bar.AddSearchBar
import com.addzero.component.table.original.ColumnConfig
import com.addzero.component.table.original.TableOriginal
import com.addzero.component.table.original.model.ColumnAlignment
import com.addzero.component.table.original.model.ColumnDataType
import com.addzero.util.str.formatCurrency
import com.addzero.util.str.kmpFormat
import kotlin.uuid.ExperimentalUuidApi

// 大数据测试数据类 - 100个字段
data class LargeDataUser(
    val id: Long,
    val name: String,
    val age: Int,
    val email: String,
    val department: String,
    val salary: Double,
    val isActive: Boolean,
    // 扩展字段 - 个人信息
    val phone: String,
    val address: String,
    val city: String,
    val country: String,
    val zipCode: String,
    val birthDate: String,
    val gender: String,
    val maritalStatus: String,
    val education: String,
    val university: String,
    val major: String,
    val graduationYear: Int,
    val workExperience: Int,
    val skills: String,
    val languages: String,
    val certifications: String,
    // 工作相关字段
    val jobTitle: String,
    val level: String,
    val manager: String,
    val team: String,
    val project: String,
    val startDate: String,
    val contractType: String,
    val workLocation: String,
    val workHours: String,
    val overtime: Double,
    val bonus: Double,
    val commission: Double,
    val benefits: String,
    val vacation: Int,
    val sickLeave: Int,
    // 绩效相关
    val performance: String,
    val rating: Double,
    val goals: String,
    val achievements: String,
    val feedback: String,
    val lastReview: String,
    val nextReview: String,
    // 财务相关
    val bankAccount: String,
    val taxId: String,
    val socialSecurity: String,
    val insurance: String,
    val retirement: String,
    val stockOptions: Int,
    val expenses: Double,
    val reimbursements: Double,
    // 技术相关
    val programmingLanguages: String,
    val frameworks: String,
    val databases: String,
    val tools: String,
    val platforms: String,
    val methodologies: String,
    val projectsCompleted: Int,
    val codeReviews: Int,
    val bugsFixed: Int,
    val featuresDelivered: Int,
    // 培训相关
    val trainingHours: Int,
    val coursesCompleted: Int,
    val conferences: String,
    val workshops: String,
    val mentoring: String,
    val publications: String,
    val patents: Int,
    // 社交相关
    val linkedin: String,
    val github: String,
    val twitter: String,
    val blog: String,
    val portfolio: String,
    // 健康相关
    val healthInsurance: String,
    val emergencyContact: String,
    val allergies: String,
    val medications: String,
    val fitnessLevel: String,
    // 兴趣爱好
    val hobbies: String,
    val sports: String,
    val music: String,
    val books: String,
    val movies: String,
    val travel: String,
    val volunteer: String,
    // 其他字段
    val notes: String,
    val tags: String,
    val priority: String,
    val status: String,
    val lastLogin: String,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String,
    val updatedBy: String,
    val version: Int,
    val archived: Boolean,
    val verified: Boolean,
    val featured: Boolean
)

@Composable
@Route("组件示例", "测试表格")
fun TableDemo() {
    var searchKeyword by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<LargeDataUser?>(null) }

    // 生成1000行大数据
    val originalData = remember {
        generateLargeDataSet(1000)
    }

    // 过滤数据
    val filteredData by remember {
        derivedStateOf {
            if (searchKeyword.isEmpty()) {
                originalData
            } else {
                originalData.filter { user ->
                    user.name.contains(searchKeyword, ignoreCase = true) ||
                    user.email.contains(searchKeyword, ignoreCase = true) ||
                    user.department.contains(searchKeyword, ignoreCase = true) ||
                    user.jobTitle.contains(searchKeyword, ignoreCase = true) ||
                    user.skills.contains(searchKeyword, ignoreCase = true)
                }
            }
        }
    }

    // 100个列定义 - 显示所有字段
    val columns = listOf(
        "name", "age", "email", "department", "salary", "isActive",
        "phone", "address", "city", "country", "zipCode", "birthDate", "gender", "maritalStatus",
        "education", "university", "major", "graduationYear", "workExperience", "skills",
        "languages", "certifications", "jobTitle", "level", "manager", "team", "project",
        "startDate", "contractType", "workLocation", "workHours", "overtime", "bonus",
        "commission", "benefits", "vacation", "sickLeave", "performance", "rating",
        "goals", "achievements", "feedback", "lastReview", "nextReview", "bankAccount",
        "taxId", "socialSecurity", "insurance", "retirement", "stockOptions", "expenses",
        "reimbursements", "programmingLanguages", "frameworks", "databases", "tools",
        "platforms", "methodologies", "projectsCompleted", "codeReviews", "bugsFixed",
        "featuresDelivered", "trainingHours", "coursesCompleted", "conferences", "workshops",
        "mentoring", "publications", "patents", "linkedin", "github", "twitter", "blog",
        "portfolio", "healthInsurance", "emergencyContact", "allergies", "medications",
        "fitnessLevel", "hobbies", "sports", "music", "books", "movies", "travel",
        "volunteer", "notes", "tags", "priority", "status", "lastLogin", "createdAt",
        "updatedAt", "createdBy", "updatedBy", "version", "archived", "verified", "featured"
    )

    // 100个列配置
    val columnConfigs = listOf(
        ColumnConfig("name", "姓名", 120f, alignment = ColumnAlignment.START),
        ColumnConfig("age", "年龄", 80f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("email", "邮箱", 200f, alignment = ColumnAlignment.START),
        ColumnConfig("department", "部门", 100f),
        ColumnConfig("salary", "薪资", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("isActive", "状态", 80f),
        ColumnConfig("phone", "电话", 120f),
        ColumnConfig("address", "地址", 200f, alignment = ColumnAlignment.START),
        ColumnConfig("city", "城市", 100f),
        ColumnConfig("country", "国家", 100f),
        ColumnConfig("zipCode", "邮编", 80f),
        ColumnConfig("birthDate", "生日", 100f),
        ColumnConfig("gender", "性别", 60f),
        ColumnConfig("maritalStatus", "婚姻状况", 100f),
        ColumnConfig("education", "学历", 100f),
        ColumnConfig("university", "大学", 150f),
        ColumnConfig("major", "专业", 120f),
        ColumnConfig("graduationYear", "毕业年份", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("workExperience", "工作经验", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("skills", "技能", 200f, alignment = ColumnAlignment.START),
        ColumnConfig("languages", "语言", 150f),
        ColumnConfig("certifications", "认证", 150f),
        ColumnConfig("jobTitle", "职位", 150f),
        ColumnConfig("level", "级别", 80f),
        ColumnConfig("manager", "经理", 100f),
        ColumnConfig("team", "团队", 100f),
        ColumnConfig("project", "项目", 150f),
        ColumnConfig("startDate", "入职日期", 100f),
        ColumnConfig("contractType", "合同类型", 100f),
        ColumnConfig("workLocation", "工作地点", 120f),
        ColumnConfig("workHours", "工作时间", 100f),
        ColumnConfig("overtime", "加班时间", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("bonus", "奖金", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("commission", "提成", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("benefits", "福利", 150f),
        ColumnConfig("vacation", "假期", 80f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("sickLeave", "病假", 80f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("performance", "绩效", 100f),
        ColumnConfig("rating", "评分", 80f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("goals", "目标", 200f, alignment = ColumnAlignment.START),
        ColumnConfig("achievements", "成就", 200f, alignment = ColumnAlignment.START),
        ColumnConfig("feedback", "反馈", 200f, alignment = ColumnAlignment.START),
        ColumnConfig("lastReview", "上次评估", 100f),
        ColumnConfig("nextReview", "下次评估", 100f),
        ColumnConfig("bankAccount", "银行账户", 150f),
        ColumnConfig("taxId", "税号", 120f),
        ColumnConfig("socialSecurity", "社保", 120f),
        ColumnConfig("insurance", "保险", 120f),
        ColumnConfig("retirement", "退休金", 100f),
        ColumnConfig("stockOptions", "股票期权", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("expenses", "费用", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("reimbursements", "报销", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("programmingLanguages", "编程语言", 150f),
        ColumnConfig("frameworks", "框架", 150f),
        ColumnConfig("databases", "数据库", 120f),
        ColumnConfig("tools", "工具", 150f),
        ColumnConfig("platforms", "平台", 120f),
        ColumnConfig("methodologies", "方法论", 120f),
        ColumnConfig("projectsCompleted", "完成项目", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("codeReviews", "代码审查", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("bugsFixed", "修复Bug", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("featuresDelivered", "交付功能", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("trainingHours", "培训时间", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("coursesCompleted", "完成课程", 100f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("conferences", "会议", 150f),
        ColumnConfig("workshops", "研讨会", 120f),
        ColumnConfig("mentoring", "指导", 120f),
        ColumnConfig("publications", "发表文章", 150f),
        ColumnConfig("patents", "专利", 80f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("linkedin", "LinkedIn", 150f),
        ColumnConfig("github", "GitHub", 150f),
        ColumnConfig("twitter", "Twitter", 120f),
        ColumnConfig("blog", "博客", 150f),
        ColumnConfig("portfolio", "作品集", 150f),
        ColumnConfig("healthInsurance", "健康保险", 120f),
        ColumnConfig("emergencyContact", "紧急联系人", 150f),
        ColumnConfig("allergies", "过敏", 120f),
        ColumnConfig("medications", "药物", 120f),
        ColumnConfig("fitnessLevel", "健身水平", 100f),
        ColumnConfig("hobbies", "爱好", 150f),
        ColumnConfig("sports", "运动", 120f),
        ColumnConfig("music", "音乐", 120f),
        ColumnConfig("books", "书籍", 150f),
        ColumnConfig("movies", "电影", 150f),
        ColumnConfig("travel", "旅行", 150f),
        ColumnConfig("volunteer", "志愿服务", 120f),
        ColumnConfig("notes", "备注", 200f, alignment = ColumnAlignment.START),
        ColumnConfig("tags", "标签", 150f),
        ColumnConfig("priority", "优先级", 80f),
        ColumnConfig("status", "状态", 80f),
        ColumnConfig("lastLogin", "最后登录", 120f),
        ColumnConfig("createdAt", "创建时间", 120f),
        ColumnConfig("updatedAt", "更新时间", 120f),
        ColumnConfig("createdBy", "创建者", 100f),
        ColumnConfig("updatedBy", "更新者", 100f),
        ColumnConfig("version", "版本", 60f, dataType = ColumnDataType.NUMBER),
        ColumnConfig("archived", "已归档", 80f),
        ColumnConfig("verified", "已验证", 80f),
        ColumnConfig("featured", "推荐", 80f)
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 页面标题
        Text(
            text = "TableOriginal 组件测试",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 统计信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("总用户", originalData.size.toString())
                StatItem("已筛选", filteredData.size.toString())
                StatItem("已选择", selectedItems.size.toString())
                StatItem("活跃用户", originalData.count { it.isActive }.toString())
            }
        }

        // TableOriginal 组件
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            TableOriginal(
                data = filteredData,
                columns = columns,
                getColumnKey = { it },
                getRowId = { it.id },
                columnConfigs = columnConfigs,
                getColumnLabel = { column ->
                    val config = columnConfigs.find { it.key == column }
                    Text(
                        text = config?.label ?: column,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                topSlot = {
                    // 搜索栏和工具栏
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AddSearchBar(
                            keyword = searchKeyword,
                            onKeyWordChanged = { searchKeyword = it },
                            onSearch = { /* 搜索逻辑已在 derivedStateOf 中处理 */ },
                            leftSloat = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AddIconButton(
                                        text = "添加用户",
                                        imageVector = Icons.Default.Add,
                                        onClick = { /* 添加用户逻辑 */ }
                                    )
                                    AddIconButton(
                                        text = "导出数据",
                                        imageVector = Icons.Default.Download,
                                        onClick = { /* 导出逻辑 */ }
                                    )
                                    if (selectedItems.isNotEmpty()) {
                                        AddIconButton(
                                            text = "批量删除",
                                            imageVector = Icons.Default.Delete,
                                            onClick = { /* 批量删除逻辑 */ }
                                        )
                                    }
                                }
                            }
                        )

                        // 选择状态提示
                        if (selectedItems.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "已选择 ${selectedItems.size} 项",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    TextButton(
                                        onClick = { selectedItems = emptySet() }
                                    ) {
                                        Text("清除选择")
                                    }
                                }
                            }
                        }
                    }
                },
                getCellContent = { user, column ->
                    getCellContentForColumn(user, column)
                },
                rowLeftSlot = { user, index ->
                    // 只显示复选框，序号由固定序号列处理
                    Checkbox(
                        checked = selectedItems.contains(user.id),
                        onCheckedChange = { checked ->
                            selectedItems = if (checked) {
                                selectedItems + user.id
                            } else {
                                selectedItems - user.id
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                },
                rowActionSlot = { user ->
                    // 行操作按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AddIconButton(
                            text = "查看",
                            imageVector = Icons.Default.Visibility,
                            onClick = {
                                println("查看用户: ${user.name}")
                            }
                        )
                        AddIconButton(
                            text = "编辑",
                            imageVector = Icons.Default.Edit,
                            onClick = {
                                println("编辑用户: ${user.name}")
                            }
                        )
                        AddIconButton(
                            text = "删除",
                            imageVector = Icons.Default.Delete,
                            onClick = {
                                itemToDelete = user
                                showDeleteDialog = true
                            }
                        )
                    }
                },
                bottomSlot = {
                    // 底部统计信息
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "显示 ${filteredData.size} 条记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val avgSalary = if (filteredData.isNotEmpty()) {
                                filteredData.map { it.salary }.average()
                            } else 0.0

                            Text(
                                text = "平均薪资: ¥${avgSalary.formatCurrency(0)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // 删除确认对话框
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除用户 \"${itemToDelete!!.name}\" 吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        println("删除用户: ${itemToDelete!!.name}")
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

// 辅助组件：统计项
@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

// 辅助组件：状态芯片
@Composable
private fun StatusChip(isActive: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFFF5722)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isActive) "活跃" else "停用",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

// 辅助组件：自定义状态芯片
@Composable
private fun StatusChip(isActive: Boolean, activeText: String, inactiveText: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isActive) activeText else inactiveText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

// 辅助函数：获取部门颜色
private fun getDepartmentColor(department: String): Color {
    return when (department) {
        "技术部" -> Color(0xFF2196F3)
        "产品部" -> Color(0xFF9C27B0)
        "设计部" -> Color(0xFFFF9800)
        "市场部" -> Color(0xFF4CAF50)
        "人事部" -> Color(0xFFE91E63)
        "财务部" -> Color(0xFF795548)
        else -> Color(0xFF607D8B)
    }
}

// 生成大数据集函数
private fun generateLargeDataSet(count: Int): List<LargeDataUser> {
    val departments = listOf("技术部", "产品部", "设计部", "市场部", "人事部", "财务部", "运营部", "销售部")
    val cities = listOf("北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉", "西安", "重庆")
    val skills = listOf("Java", "Kotlin", "Python", "JavaScript", "React", "Vue", "Spring", "MySQL", "Redis", "Docker")
    val jobTitles = listOf("软件工程师", "高级工程师", "架构师", "产品经理", "设计师", "测试工程师", "运维工程师", "数据分析师")

    return (1..count).map { i ->
        LargeDataUser(
            id = i.toLong(),
            name = "用户$i",
            age = (22..60).random(),
            email = "user$i@example.com",
            department = departments.random(),
            salary = (8000..50000).random().toDouble(),
            isActive = (i % 10) != 0, // 90% 活跃
            phone = "1${(3..9).random()}${(0..9).random()}${(0..99999999).random().toString().padStart(8, '0')}",
            address = "地址",
            city = cities.random(),
            country = "中国",
            zipCode = (100000..999999).random().toString(),
            birthDate = "19${(70..99).random()}-${(1..12).random().toString().padStart(2, '0')}-${(1..28).random().toString().padStart(2, '0')}",
            gender = if (i % 2 == 0) "男" else "女",
            maritalStatus = if (i % 3 == 0) "已婚" else "未婚",
            education = listOf("本科", "硕士", "博士", "专科").random(),
            university = "大学$i",
            major = "专业$i",
            graduationYear = (2010..2023).random(),
            workExperience = (0..15).random(),
            skills = skills.shuffled().take((1..5).random()).joinToString(", "),
            languages = listOf("中文", "英语", "日语", "韩语").shuffled().take((1..3).random()).joinToString(", "),
            certifications = "认证$i",
            jobTitle = jobTitles.random(),
            level = listOf("初级", "中级", "高级", "专家").random(),
            manager = "经理${(i % 10) + 1}",
            team = "团队${(i % 5) + 1}",
            project = "项目$i",
            startDate = "202${(0..3).random()}-${(1..12).random().toString().padStart(2, '0')}-${(1..28).random().toString().padStart(2, '0')}",
            contractType = listOf("全职", "兼职", "合同工", "实习").random(),
            workLocation = cities.random(),
            workHours = "9:00-18:00",
            overtime = (0..50).random().toDouble(),
            bonus = (0..20000).random().toDouble(),
            commission = (0..10000).random().toDouble(),
            benefits = "福利包$i",
            vacation = (5..30).random(),
            sickLeave = (0..10).random(),
            performance = listOf("优秀", "良好", "一般", "待改进").random(),
            rating = 5.0,
            goals = "目标$i",
            achievements = "成就$i",
            feedback = "反馈$i",
            lastReview = "2023-${(1..12).random().toString().padStart(2, '0')}-01",
            nextReview = "2024-${(1..12).random().toString().padStart(2, '0')}-01",
            bankAccount = "62${(0..9999999999999999L).random().toString().padStart(16, '0')}",
            taxId = (0..999999999999999999L).random().toString().padStart(18, '0'),
            socialSecurity = (0..999999999999L).random().toString().padStart(12, '0'),
            insurance = "保险$i",
            retirement = "退休金$i",
            stockOptions = (0..10000).random(),
            expenses = (0..5000).random().toDouble(),
            reimbursements = (0..3000).random().toDouble(),
            programmingLanguages = skills.shuffled().take((1..4).random()).joinToString(", "),
            frameworks = listOf("Spring", "React", "Vue", "Angular", "Django", "Flask").shuffled().take((1..3).random()).joinToString(", "),
            databases = listOf("MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle").shuffled().take((1..3).random()).joinToString(", "),
            tools = listOf("Git", "Docker", "Jenkins", "JIRA", "Confluence").shuffled().take((1..4).random()).joinToString(", "),
            platforms = listOf("Linux", "Windows", "macOS", "AWS", "Azure").shuffled().take((1..3).random()).joinToString(", "),
            methodologies = listOf("Agile", "Scrum", "Kanban", "DevOps").shuffled().take((1..2).random()).joinToString(", "),
            projectsCompleted = (0..50).random(),
            codeReviews = (0..200).random(),
            bugsFixed = (0..100).random(),
            featuresDelivered = (0..30).random(),
            trainingHours = (0..100).random(),
            coursesCompleted = (0..20).random(),
            conferences = "会议$i",
            workshops = "研讨会$i",
            mentoring = "指导$i",
            publications = "发表$i",
            patents = (0..5).random(),
            linkedin = "linkedin.com/in/user$i",
            github = "github.com/user$i",
            twitter = "@user$i",
            blog = "blog.user$i.com",
            portfolio = "portfolio.user$i.com",
            healthInsurance = "健康保险$i",
            emergencyContact = "紧急联系人$i",
            allergies = if (i % 10 == 0) "花粉过敏" else "无",
            medications = if (i % 15 == 0) "药物$i" else "无",
            fitnessLevel = listOf("优秀", "良好", "一般", "较差").random(),
            hobbies = listOf("阅读", "运动", "音乐", "旅行", "摄影").shuffled().take((1..3).random()).joinToString(", "),
            sports = listOf("跑步", "游泳", "篮球", "足球", "网球").shuffled().take((1..2).random()).joinToString(", "),
            music = listOf("流行", "古典", "摇滚", "爵士").random(),
            books = "书籍$i",
            movies = "电影$i",
            travel = "旅行$i",
            volunteer = if (i % 5 == 0) "志愿服务$i" else "无",
            notes = "备注$i",
            tags = listOf("重要", "优先", "关注", "新人").shuffled().take((0..2).random()).joinToString(", "),
            priority = listOf("高", "中", "低").random(),
            status = listOf("正常", "试用", "离职", "休假").random(),
            lastLogin = "2023-12-${(1..31).random().toString().padStart(2, '0')} ${(0..23).random().toString().padStart(2, '0')}:${(0..59).random().toString().padStart(2, '0')}",
            createdAt = "2023-01-${(1..31).random().toString().padStart(2, '0')} 10:00:00",
            updatedAt = "2023-12-${(1..31).random().toString().padStart(2, '0')} ${(0..23).random().toString().padStart(2, '0')}:${(0..59).random().toString().padStart(2, '0')}",
            createdBy = "admin",
            updatedBy = "user${(i % 10) + 1}",
            version = (1..10).random(),
            archived = i % 50 == 0, // 2% 归档
            verified = i % 3 != 0, // 67% 验证
            featured = i % 20 == 0 // 5% 推荐
        )
    }
}

// 处理所有字段的单元格内容
@Composable
private fun getCellContentForColumn(user: LargeDataUser, column: String) {
    when (column) {
        "name" -> Text(text = user.name, fontWeight = FontWeight.Medium)
        "age" -> Text(text = user.age.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        "email" -> Text(text = user.email, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        "department" -> Card(
            colors = CardDefaults.cardColors(containerColor = getDepartmentColor(user.department)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = user.department,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        "salary" -> Text(
            text = "¥${user.salary.formatCurrency(0)}",
            fontWeight = FontWeight.Bold,
            color = if (user.salary > 15000) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
        "isActive" -> StatusChip(user.isActive)
        "phone" -> Text(text = user.phone, style = MaterialTheme.typography.bodySmall)
        "address" -> Text(text = user.address, style = MaterialTheme.typography.bodySmall)
        "city" -> Text(text = user.city)
        "country" -> Text(text = user.country)
        "zipCode" -> Text(text = user.zipCode)
        "birthDate" -> Text(text = user.birthDate, style = MaterialTheme.typography.bodySmall)
        "gender" -> Text(text = user.gender)
        "maritalStatus" -> Text(text = user.maritalStatus)
        "education" -> Text(text = user.education, fontWeight = FontWeight.Medium)
        "university" -> Text(text = user.university, style = MaterialTheme.typography.bodySmall)
        "major" -> Text(text = user.major)
        "graduationYear" -> Text(text = user.graduationYear.toString())
        "workExperience" -> Text(text = "${user.workExperience}年", color = MaterialTheme.colorScheme.primary)
        "skills" -> Text(text = user.skills, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "languages" -> Text(text = user.languages, style = MaterialTheme.typography.bodySmall)
        "certifications" -> Text(text = user.certifications, style = MaterialTheme.typography.bodySmall)
        "jobTitle" -> Text(text = user.jobTitle, fontWeight = FontWeight.Medium)
        "level" -> Card(
            colors = CardDefaults.cardColors(
                containerColor = when (user.level) {
                    "初级" -> Color(0xFF9E9E9E)
                    "中级" -> Color(0xFF2196F3)
                    "高级" -> Color(0xFF4CAF50)
                    "专家" -> Color(0xFFFF9800)
                    else -> Color(0xFF607D8B)
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = user.level,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        "manager" -> Text(text = user.manager)
        "team" -> Text(text = user.team)
        "project" -> Text(text = user.project, style = MaterialTheme.typography.bodySmall)
        "startDate" -> Text(text = user.startDate, style = MaterialTheme.typography.bodySmall)
        "contractType" -> Text(text = user.contractType)
        "workLocation" -> Text(text = user.workLocation)
        "workHours" -> Text(text = user.workHours, style = MaterialTheme.typography.bodySmall)
        "overtime" -> Text(text = "${user.overtime}h", color = if (user.overtime > 20) Color(0xFFFF5722) else MaterialTheme.colorScheme.onSurface)
        "bonus" -> Text(text = "¥${user.bonus.formatCurrency(0)}", color = Color(0xFF4CAF50))
        "commission" -> Text(text = "¥${user.commission.formatCurrency(0)}", color = Color(0xFF4CAF50))
        "benefits" -> Text(text = user.benefits, style = MaterialTheme.typography.bodySmall)
        "vacation" -> Text(text = "${user.vacation}天")
        "sickLeave" -> Text(text = "${user.sickLeave}天")
        "performance" -> Card(
            colors = CardDefaults.cardColors(
                containerColor = when (user.performance) {
                    "优秀" -> Color(0xFF4CAF50)
                    "良好" -> Color(0xFF2196F3)
                    "一般" -> Color(0xFFFF9800)
                    "待改进" -> Color(0xFFFF5722)
                    else -> Color(0xFF9E9E9E)
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = user.performance,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        "rating" -> Text(text = "%.1f".kmpFormat(user.rating), fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
        "goals" -> Text(text = user.goals, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "achievements" -> Text(text = user.achievements, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "feedback" -> Text(text = user.feedback, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "lastReview" -> Text(text = user.lastReview, style = MaterialTheme.typography.bodySmall)
        "nextReview" -> Text(text = user.nextReview, style = MaterialTheme.typography.bodySmall)
        "bankAccount" -> Text(text = "****${user.bankAccount.takeLast(4)}", style = MaterialTheme.typography.bodySmall)
        "taxId" -> Text(text = "****${user.taxId.takeLast(4)}", style = MaterialTheme.typography.bodySmall)
        "socialSecurity" -> Text(text = "****${user.socialSecurity.takeLast(4)}", style = MaterialTheme.typography.bodySmall)
        "insurance" -> Text(text = user.insurance, style = MaterialTheme.typography.bodySmall)
        "retirement" -> Text(text = user.retirement, style = MaterialTheme.typography.bodySmall)
        "stockOptions" -> Text(text = user.stockOptions.toString(), color = Color(0xFF4CAF50))
        "expenses" -> Text(text = "¥${user.expenses.formatCurrency(0)}")
        "reimbursements" -> Text(text = "¥${user.reimbursements.formatCurrency(0)}")
        "programmingLanguages" -> Text(text = user.programmingLanguages, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "frameworks" -> Text(text = user.frameworks, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "databases" -> Text(text = user.databases, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "tools" -> Text(text = user.tools, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "platforms" -> Text(text = user.platforms, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "methodologies" -> Text(text = user.methodologies, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "projectsCompleted" -> Text(text = user.projectsCompleted.toString(), color = Color(0xFF4CAF50))
        "codeReviews" -> Text(text = user.codeReviews.toString())
        "bugsFixed" -> Text(text = user.bugsFixed.toString(), color = Color(0xFFFF9800))
        "featuresDelivered" -> Text(text = user.featuresDelivered.toString(), color = Color(0xFF4CAF50))
        "trainingHours" -> Text(text = "${user.trainingHours}h")
        "coursesCompleted" -> Text(text = user.coursesCompleted.toString())
        "conferences" -> Text(text = user.conferences, style = MaterialTheme.typography.bodySmall)
        "workshops" -> Text(text = user.workshops, style = MaterialTheme.typography.bodySmall)
        "mentoring" -> Text(text = user.mentoring, style = MaterialTheme.typography.bodySmall)
        "publications" -> Text(text = user.publications, style = MaterialTheme.typography.bodySmall)
        "patents" -> Text(text = user.patents.toString(), color = Color(0xFFFF9800))
        "linkedin" -> Text(text = user.linkedin, style = MaterialTheme.typography.bodySmall, color = Color(0xFF0077B5))
        "github" -> Text(text = user.github, style = MaterialTheme.typography.bodySmall, color = Color(0xFF333333))
        "twitter" -> Text(text = user.twitter, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1DA1F2))
        "blog" -> Text(text = user.blog, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        "portfolio" -> Text(text = user.portfolio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        "healthInsurance" -> Text(text = user.healthInsurance, style = MaterialTheme.typography.bodySmall)
        "emergencyContact" -> Text(text = user.emergencyContact, style = MaterialTheme.typography.bodySmall)
        "allergies" -> Text(text = user.allergies, style = MaterialTheme.typography.bodySmall, color = if (user.allergies != "无") Color(0xFFFF5722) else MaterialTheme.colorScheme.onSurface)
        "medications" -> Text(text = user.medications, style = MaterialTheme.typography.bodySmall)
        "fitnessLevel" -> Text(text = user.fitnessLevel)
        "hobbies" -> Text(text = user.hobbies, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        "sports" -> Text(text = user.sports, style = MaterialTheme.typography.bodySmall)
        "music" -> Text(text = user.music, style = MaterialTheme.typography.bodySmall)
        "books" -> Text(text = user.books, style = MaterialTheme.typography.bodySmall)
        "movies" -> Text(text = user.movies, style = MaterialTheme.typography.bodySmall)
        "travel" -> Text(text = user.travel, style = MaterialTheme.typography.bodySmall)
        "volunteer" -> Text(text = user.volunteer, style = MaterialTheme.typography.bodySmall)
        "notes" -> Text(text = user.notes, style = MaterialTheme.typography.bodySmall, maxLines = 3)
        "tags" -> Text(text = user.tags, style = MaterialTheme.typography.bodySmall)
        "priority" -> Card(
            colors = CardDefaults.cardColors(
                containerColor = when (user.priority) {
                    "高" -> Color(0xFFFF5722)
                    "中" -> Color(0xFFFF9800)
                    "低" -> Color(0xFF4CAF50)
                    else -> Color(0xFF9E9E9E)
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = user.priority,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        "status" -> Text(text = user.status)
        "lastLogin" -> Text(text = user.lastLogin, style = MaterialTheme.typography.bodySmall)
        "createdAt" -> Text(text = user.createdAt, style = MaterialTheme.typography.bodySmall)
        "updatedAt" -> Text(text = user.updatedAt, style = MaterialTheme.typography.bodySmall)
        "createdBy" -> Text(text = user.createdBy, style = MaterialTheme.typography.bodySmall)
        "updatedBy" -> Text(text = user.updatedBy, style = MaterialTheme.typography.bodySmall)
        "version" -> Text(text = "v${user.version}", style = MaterialTheme.typography.bodySmall)
        "archived" -> StatusChip(user.archived, "已归档", "正常")
        "verified" -> StatusChip(user.verified, "已验证", "未验证")
        "featured" -> StatusChip(user.featured, "推荐", "普通")
        else -> Text(text = "N/A", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
