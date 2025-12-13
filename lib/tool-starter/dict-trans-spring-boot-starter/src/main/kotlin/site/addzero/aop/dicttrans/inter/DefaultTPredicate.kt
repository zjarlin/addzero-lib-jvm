package site.addzero.aop.dicttrans.inter

import org.springframework.stereotype.Component

/**
 * Default implementation of TPredicate
 *
 * @author zjarlin
 * @since 2025/01/12
 */
@Component
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
            java.math.BigDecimal::class.java
        )
    }
}