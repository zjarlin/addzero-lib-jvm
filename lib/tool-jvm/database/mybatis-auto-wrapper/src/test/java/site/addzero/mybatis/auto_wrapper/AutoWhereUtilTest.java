package site.addzero.mybatis.auto_wrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试 - AutoWhereUtil
 */
class AutoWhereUtilTest {




    // 测试DTO - 空值处理
    static class UserNullDTO {
        @Where(value = "null")
        @Where
        private String email;

        @Where(value = "notNull")
        private String status;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    @Test
    void testQueryByField_columnNameMapping() {
        // 测试字段名映射
        UserNullDTO dto = new UserNullDTO();

        dto.setEmail("测试");

        QueryWrapper<UserNullDTO> wrapper = AutoWhereUtil.queryByField(UserNullDTO.class, dto);



        UserNullDTO dto1 = new UserNullDTO();
        QueryWrapper<UserNullDTO> wrapper1 = AutoWhereUtil.queryByField(UserNullDTO.class, dto1);




        assertNotNull(wrapper);

        String sqlSegment = wrapper.getSqlSegment();
        String sqlSegment1 = wrapper1.getSqlSegment();
        System.out.println();
    }
}
