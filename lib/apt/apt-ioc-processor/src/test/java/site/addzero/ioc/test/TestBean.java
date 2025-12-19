package site.addzero.ioc.test;
import site.addzero.ioc.annotation.Bean;

public class TestBean {

    @Bean
    public static void staticBeanMethod() {
        System.out.println("Static bean method executed");
    }

    @Bean
    public void instanceBeanMethod() {
        System.out.println("Instance bean method executed");
    }
}