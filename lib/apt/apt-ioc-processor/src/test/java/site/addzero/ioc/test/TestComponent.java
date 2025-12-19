package site.addzero.ioc.test;

import site.addzero.ioc.annotation.Component;

@Component
public class TestComponent {

    public String testMethod() {
        return "TestComponent executed";
    }
}