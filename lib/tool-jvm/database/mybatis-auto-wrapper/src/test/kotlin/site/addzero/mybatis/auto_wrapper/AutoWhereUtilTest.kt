package site.addzero.mybatis.auto_wrapper

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * 单元测试 - AutoWhereUtil
 */
internal class AutoWhereUtilTest {
    // 测试DTO - 空值处理
    internal class UserNullDTO {
        @Where(value = "null")
        @Where
        var email: String? = null

        @Where(value = "notNull")
        var status: String? = null
    }


    @Test
    fun testQueryByField_columnNameMapping() {
        // 测试场景1：email 有值时，应该只生成 = 条件，不生成 IS NULL
        val dto = UserNullDTO()
        dto.email = "测试"
        val wrapper = queryByField(UserNullDTO::class.java, dto)
        val sqlSegment = wrapper.sqlSegment
        println("场景1 - email有值: $sqlSegment")
        // 期望：只有 email = '测试'，没有 IS NULL
        Assertions.assertTrue(sqlSegment.contains("email ="), "应该包含 email = 条件")
        Assertions.assertFalse(sqlSegment.contains("IS NULL"), "不应该包含 IS NULL 条件")
        // 测试场景2：email 为 null 时，应该只生成 IS NULL 条件，不生成 = 条件
        val dto1 = UserNullDTO()
        val wrapper1 = queryByField(UserNullDTO::class.java, dto1)
        val sqlSegment1 = wrapper1.sqlSegment
        println("场景2 - email为null: $sqlSegment1")
        // 期望：只有 email IS NULL，没有 email =
        Assertions.assertTrue(sqlSegment1.contains("email IS NULL"), "应该包含 IS NULL 条件")
        Assertions.assertFalse(sqlSegment1.contains("email =") && sqlSegment1.contains("MPGENVAL"), "不应该同时包含 = 条件")
        // 测试场景3：status 使用 notNull，只有当值不为 null 时才应用
        val dto2 = UserNullDTO()
        dto2.status = "active"
        val wrapper2 = queryByField(UserNullDTO::class.java, dto2)
        val sqlSegment2 = wrapper2.sqlSegment
        println("场景3 - status有值: $sqlSegment2")
        Assertions.assertTrue(sqlSegment2.contains("status IS NOT NULL"), "应该包含 IS NOT NULL 条件")
    }
}
