package site.addzero.mybatis.auto_wrapper

import com.baomidou.mybatisplus.annotation.TableField
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * 单元测试 - AutoWhereUtil
 */
internal class AutoWhereUtilTest {

   internal class IotEventPropertyMapping  {
        @TableField("event_metadata_id")
         val eventMetadataId: Long? = null

        @TableField("property_metadata_id")
        var propertyMetadataId: Long? = null
    }


    // 测试DTO - 空值处理
    internal class UserNullDTO {
        @Where(value = "null")
        @Where
        var email: String? = null

        @Where(value = "notNull")
        var status: String? = null
    }

    internal class IgnoreWhereDTO {
        @Where(ignore = true)
        var phone: String? = null

        @Where
        var name: String? = null
    }

    // 测试DTO - SpEL表达式处理
    internal class UserSpelDTO {
        @Where(condition = "#value != null && #value.startsWith('test')")
        var nickname: String = "test"

        @Where(value = "null", condition = "#dto.requireNull == true")
        var deletedAt: String? = null

        var requireNull: Boolean = false
    }

    // 测试DTO - in/notIn & findInSet
    internal class UserInDTO {
        @Where(value = "in")
        var categoryIds: String? = null

        @Where(value = "notIn")
        var excludeIds: String? = null

        @Where(value = "findInSet")
        var channels: String? = null
    }

    @Test
    fun testPro(): Unit {
        val dto = IotEventPropertyMapping()
        dto.propertyMetadataId = 1
        val wrapper = queryByField(IotEventPropertyMapping::class.java, dto)
        val sqlSegment = wrapper.sqlSegment
        assertTrue { sqlSegment.isNotBlank() }
        println()
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

    @Test
    fun testWhereIgnoreSkipCondition() {
        val dto = IgnoreWhereDTO()
        dto.phone = "123456789"
        dto.name = "tester"
        val wrapper = queryByField(IgnoreWhereDTO::class.java, dto)
        val sqlSegment = wrapper.sqlSegment
        Assertions.assertTrue(sqlSegment.contains("name ="), "未忽略字段应该生成查询条件")
        Assertions.assertFalse(sqlSegment.contains("phone"), "ignore=true 的字段不应该生成查询条件")

        val dtoOnlyIgnored = IgnoreWhereDTO()
        dtoOnlyIgnored.phone = "123456789"
        val wrapperOnlyIgnored = queryByField(IgnoreWhereDTO::class.java, dtoOnlyIgnored)
        Assertions.assertTrue(wrapperOnlyIgnored.sqlSegment.isBlank(), "只有 ignore 字段时应不生成 SQL 片段")
    }

    @Test
    fun testWhereSpelCondition() {
        // 场景1：nickname 长度满足 SpEL 条件
        val dto = UserSpelDTO()
        dto.nickname = "tester"
        val wrapper = queryByField(UserSpelDTO::class.java, dto)
        val sqlSegment = wrapper.sqlSegment
        println("SpEL 场景1 - nickname 合规: $sqlSegment")
        Assertions.assertTrue(sqlSegment.contains("nickname ="), "SpEL 为 true 时应生成 nickname 条件")
        Assertions.assertFalse(sqlSegment.contains("deleted_at"), "未触发 requireNull 不应生成 deleted_at 条件")

        // 场景2：nickname 不满足 SpEL 条件
        val dtoShort = UserSpelDTO()
        dtoShort.nickname = "abc"
        val wrapperShort = queryByField(UserSpelDTO::class.java, dtoShort)
        val sqlSegmentShort = wrapperShort.sqlSegment
        println("SpEL 场景2 - nickname 不合规: $sqlSegmentShort")
        Assertions.assertFalse(sqlSegmentShort.contains("nickname ="), "SpEL 为 false 时不应生成 nickname 条件")

        // 场景3：依赖 dto 字段的 SpEL 条件
        val dtoNull = UserSpelDTO()
        dtoNull.requireNull = true
        val wrapperNull = queryByField(UserSpelDTO::class.java, dtoNull)
        val sqlSegmentNull = wrapperNull.sqlSegment
        println("SpEL 场景3 - requireNull 触发: $sqlSegmentNull")
        Assertions.assertTrue(sqlSegmentNull.contains("deleted_at IS NULL"), "SpEL 依赖 dto 字段时应生成 IS NULL 条件")
    }

    @Test
    fun testWhereInWithCommaSeparatedString() {
        val dto = UserInDTO()
        dto.categoryIds = "1,2, 3"
        dto.excludeIds = "4,5"
        val wrapper = queryByField(UserInDTO::class.java, dto)
        val sqlSegment = wrapper.sqlSegment
        println("IN 场景 - 逗号分隔: $sqlSegment")
        Assertions.assertTrue(sqlSegment.contains("category_ids IN"), "逗号分隔字符串应被拆分成 IN 条件")
        Assertions.assertTrue(sqlSegment.contains("exclude_ids NOT IN"), "逗号分隔字符串应被拆分成 NOT IN 条件")
    }

    @Test
    fun testWhereFindInSetCondition() {
        val dto = UserInDTO()
        dto.channels = "wx, dy"
        val wrapper = queryByField(UserInDTO::class.java, dto)
        val sqlSegment = wrapper.sqlSegment
        println("FIND_IN_SET 场景: $sqlSegment")
        Assertions.assertTrue(sqlSegment.contains("FIND_IN_SET"), "findInSet 运算符应生成 FIND_IN_SET 语句")
        Assertions.assertTrue(sqlSegment.contains("channel"), "findInSet 应作用于字段列")
    }
}
