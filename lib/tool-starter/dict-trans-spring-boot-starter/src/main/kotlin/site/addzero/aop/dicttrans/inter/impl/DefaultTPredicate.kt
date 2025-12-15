package site.addzero.aop.dicttrans.inter.impl

import site.addzero.aop.dicttrans.inter.TPredicate
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

/**
 * 默认的T类型判断实现
 * 
 * 只有在用户没有提供自己的TPredicate实现时才会被注册
 * 
 * @author zjarlin
 * @since 2025/01/01
 */
@Component
@ConditionalOnMissingBean(TPredicate::class)
class DefaultTPredicate : TPredicate {
    
    override fun tBlackList(): List<Class<out Any>> {
        return listOf(
            String::class.java,
            Number::class.java,
            Boolean::class.java,
            Char::class.java,
            java.util.Date::class.java,
            java.time.LocalDate::class.java,
            java.time.LocalDateTime::class.java,
            java.time.LocalTime::class.java,
            java.math.BigDecimal::class.java,
            java.math.BigInteger::class.java,
            // 添加其他基本类型和常用不可变类型
            Enum::class.java
        )
    }
}