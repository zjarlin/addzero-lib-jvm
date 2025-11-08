package site.addzero.mybatis.auto_wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xiaokedamowang
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Wheres {
    String group() default "";
    Where[] value() default {};
    boolean outerJoin() default false;
    boolean innerJoin() default false;
}
