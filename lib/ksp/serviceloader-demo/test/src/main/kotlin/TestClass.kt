package site.addzero.test

/**
 * Test class to trigger ServiceLoader processing
 */
class TestService {
    fun testMethod(): String {
        return "Hello from TestService"
    }
}

/**
 * Another test class
 */
class AnotherTestClass {
    val property: String = "test"
}

/**
 * Test annotation class
 */
@Target(AnnotationTarget.CLASS)
annotation class TestAnnotation(val value: String = "")

@TestAnnotation("sample")
class AnnotatedTestClass