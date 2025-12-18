package test;

import site.addzero.ioc.annotation.Bean;
import site.addzero.ioc.annotation.Component;

@Component
public class Test {

    @Bean
    public static void staticBean() {
        System.out.println("Static bean executed");
    }

    @Bean
    public void instanceBean() {
        System.out.println("Instance bean executed");
    }
}