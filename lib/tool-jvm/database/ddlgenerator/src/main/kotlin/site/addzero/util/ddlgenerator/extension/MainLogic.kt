package site.addzero.util.ddlgenerator.extension

import org.babyfish.jimmer.config.autoddl.Settings
import site.addzero.util.ddlgenerator.diff.comparator.*
import site.addzero.util.lsi.clazz.LsiClass

/**
 * 差量 DDL 生成结果
 */
data class DiffDdlResult(
    /** 需要创建的表 */
    val tablesToCreate: List<String>,
    /** 需要添加的列 */
    val columnsToAdd: List<String>,
    /** 需要添加的索引 */
    val indexesToAdd: List<String>,
    /** 需要添加的外键 */
    val foreignKeysToAdd: List<String>,
    /** 需要删除的外键（可选） */
    val foreignKeysToDrop: List<String>,
    /** 所有 DDL 语句（按执行顺序） */
    val allStatements: List<String>
)

/**
 * 生成差量 DDL
 *
 * 执行顺序：
 * 1. 创建缺失的表
 * 2. 添加缺失的列
 * 3. 删除多余的外键（如果允许）
 * 4. 添加缺失的索引
 * 5. 添加缺失的外键
 *
 * @return 差量 DDL 结果
 */
fun List<LsiClass>.generateDiffDdl(): DiffDdlResult {
    // 1. 找出需要创建的表
    val tablesToCreate = diffTable()

    // 2. 找出需要添加的列
    val columnsToAdd = diffColumn()

    // 3. 找出需要删除的外键（如果允许删除）
    val foreignKeysToDrop = if (Settings.autoddlAllowDeleteColumn) {
        diffDropForeignKeys()
    } else {
        emptyList()
    }

    // 4. 找出需要添加的索引
    val indexesToAdd = if (Settings.autoddlKeys) {
        diffUniqueIndexes()
    } else {
        emptyList()
    }

    // 5. 找出需要添加的外键
    val foreignKeysToAdd = if (Settings.autoddlForeignKeys) {
        diffForeignKeys()
    } else {
        emptyList()
    }

    // 组织所有语句（按执行顺序）
    val allStatements = buildList {
        // Phase 1: 创建表
        if (tablesToCreate.isNotEmpty()) {
            add("-- =============================================")
            add("-- Phase 1: Create Missing Tables")
            add("-- =============================================")
            addAll(tablesToCreate)
            add("")
        }

        // Phase 2: 添加列
        if (columnsToAdd.isNotEmpty()) {
            add("-- =============================================")
            add("-- Phase 2: Add Missing Columns")
            add("-- =============================================")
            addAll(columnsToAdd)
            add("")
        }

        // Phase 3: 删除外键（必须在添加索引和外键之前）
        if (foreignKeysToDrop.isNotEmpty()) {
            add("-- =============================================")
            add("-- Phase 3: Drop Obsolete Foreign Keys")
            add("-- =============================================")
            addAll(foreignKeysToDrop)
            add("")
        }

        // Phase 4: 添加索引
        if (indexesToAdd.isNotEmpty()) {
            add("-- =============================================")
            add("-- Phase 4: Add Missing Indexes")
            add("-- =============================================")
            addAll(indexesToAdd)
            add("")
        }

        // Phase 5: 添加外键
        if (foreignKeysToAdd.isNotEmpty()) {
            add("-- =============================================")
            add("-- Phase 5: Add Missing Foreign Keys")
            add("-- =============================================")
            addAll(foreignKeysToAdd)
            add("")
        }
    }

    return DiffDdlResult(
        tablesToCreate = tablesToCreate,
        columnsToAdd = columnsToAdd,
        indexesToAdd = indexesToAdd,
        foreignKeysToAdd = foreignKeysToAdd,
        foreignKeysToDrop = foreignKeysToDrop,
        allStatements = allStatements
    )
}

/**
 * 将差量 DDL 结果转换为可执行的 SQL 字符串
 */
fun DiffDdlResult.toSql(): String =
    allStatements.joinToString("\n")

/**
 * 打印差量 DDL 统计信息
 */
fun DiffDdlResult.printSummary() {
    println("=".repeat(60))
    println("Diff DDL Summary")
    println("=".repeat(60))
    println("Tables to create:      ${tablesToCreate.size}")
    println("Columns to add:        ${columnsToAdd.size}")
    println("Indexes to add:        ${indexesToAdd.size}")
    println("Foreign keys to add:   ${foreignKeysToAdd.size}")
    println("Foreign keys to drop:  ${foreignKeysToDrop.size}")
    println("Total statements:      ${allStatements.filter { !it.startsWith("--") && it.isNotBlank() }.size}")
    println("=".repeat(60))
}

/**
 * 主逻辑入口 - 生成差量 DDL 并打印统计信息
 *
 * 此函数提供了最简单的使用方式，自动生成差量 DDL 并打印统计信息
 *
 * 使用示例：
 * ```kotlin
 * val lsiClasses: List<LsiClass> = scanEntities()
 *
 * // 方式 1: 使用主逻辑函数（自动打印统计）
 * val sql = mainLogic(lsiClasses)
 * File("diff.sql").writeText(sql)
 *
 * // 方式 2: 手动控制流程
 * val diffResult = lsiClasses.generateDiffDdl()
 * diffResult.printSummary()
 * val sql = diffResult.toSql()
 *
 * // 方式 3: 使用扩展函数（推荐，定义在 EnhancedDdlExtensions.kt）
 * val sql = lsiClasses.toDiffDDL()
 * ```
 *
 * @param lsiClasses 从代码中扫描得到的实体类列表
 * @return 生成的 DDL SQL 字符串
 */
fun mainLogic(lsiClasses: List<LsiClass>): String =
    lsiClasses.generateDiffDdl()
        .also { it.printSummary() }
        .toSql()
