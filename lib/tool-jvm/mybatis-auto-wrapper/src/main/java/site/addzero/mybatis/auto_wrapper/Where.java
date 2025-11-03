package site.addzero.mybatis.auto_wrapper;


import java.lang.annotation.*;

/**
 * @author xiaokedamowang
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Wheres.class)
public @interface Where{

    String value() default "=";
    String column() default "";
    boolean join() default false;
}
