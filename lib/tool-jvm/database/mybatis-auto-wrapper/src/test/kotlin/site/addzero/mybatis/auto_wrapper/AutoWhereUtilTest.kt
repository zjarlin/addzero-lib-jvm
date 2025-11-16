package site.addzero.mybatis.auto_wrapper

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
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
        // 测试字段名映射
        val dto = UserNullDTO()

        dto.email = "测试"

        val wrapper = queryByField(UserNullDTO::class.java, dto)


        val dto1 = UserNullDTO()
        val wrapper1 =queryByField(UserNullDTO::class.java, dto1)




        Assertions.assertNotNull(wrapper)

        val sqlSegment = wrapper.getSqlSegment()
        val sqlSegment1 = wrapper1.getSqlSegment()
        println()
    }
}
