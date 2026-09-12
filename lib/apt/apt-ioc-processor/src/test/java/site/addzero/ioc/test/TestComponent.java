package site.addzero.ioc.test;

import site.addzero.ioc.annotation.Bean;

@Bean
public class TestComponent {

    public String testMethod() {
        return "TestComponent executed";
    }
}
