package site.addzero.ioc.test

import site.addzero.ioc.annotation.Component
import site.addzero.ioc.registry.BeanRegistry

@Component
class TestService {
    fun getMessage(): String = "Hello from TestService"
}

@Component("customService")
class CustomService {
    fun getCustomMessage(): String = "Hello from CustomService"
}

interface TestInterface {
    fun getValue(): String
}

@Component
class TestInterfaceImpl : TestInterface {
    override fun getValue(): String = "Interface Implementation"
}

// 抽象类测试示例
interface DataSource {
    fun getData(): String
}

abstract class AbstractDataSource : DataSource {
    protected abstract val prefix: String

    override fun getData(): String = "$prefix: Data from abstract source"
}

@Component
class DatabaseDataSource : AbstractDataSource() {
    override val prefix = "DB"
}

// 多接口继承测试
interface Cacheable {
    fun cache(): String
}

abstract class AbstractService : DataSource, Cacheable {
    protected abstract val serviceName: String
    protected abstract val prefix: String

    override fun cache(): String = "Cached by $serviceName"
}

@Component
class UserServiceImpl : AbstractService() {
    override val prefix = "USER"
    override val serviceName = "UserService"
}

fun testGeneratedRegistry(registry: BeanRegistry) {
    // 测试获取组件
    val testService = registry.getBean<TestService>()
    assert(testService != null) { "TestService should be registered" }
    assert(testService?.getMessage() == "Hello from TestService") {
        "Unexpected message from TestService"
    }

    // 测试自定义名称的组件
    val customService = registry.getBean<CustomService>()
    assert(customService != null) { "CustomService should be registered" }
    assert(customService?.getCustomMessage() == "Hello from CustomService") {
        "Unexpected message from CustomService"
    }

    // 测试接口实现
    val interfaceImpls = registry.injectList<TestInterface>()
    assert(interfaceImpls.isNotEmpty()) { "Should have at least one TestInterface implementation" }
    assert(interfaceImpls.first().getValue() == "Interface Implementation") {
        "Unexpected value from TestInterface implementation"
    }

    // 测试抽象类接口继承 - DataSource接口
    val dataSources = registry.injectList<DataSource>()
    assert(dataSources.isNotEmpty()) { "Should have DataSource implementations from abstract class inheritance" }

    // 验证DatabaseDataSource正确注册
    val dbDataSource = registry.getBean<DatabaseDataSource>()
    assert(dbDataSource != null) { "DatabaseDataSource should be registered" }
    assert(dbDataSource?.getData() == "DB: Data from abstract source") {
        "Unexpected data from DatabaseDataSource"
    }

    // 验证UserServiceImpl同时实现了DataSource和Cacheable
    val userService = registry.getBean<UserServiceImpl>()
    assert(userService != null) { "UserServiceImpl should be registered" }
    assert(userService?.getData() == "USER: User data from service") {
        "Unexpected data from UserServiceImpl"
    }
    assert(userService?.cache() == "Cached by UserService") {
        "Unexpected cache result from UserServiceImpl"
    }

    // 测试多接口实现注入
    val cacheables = registry.injectList<Cacheable>()
    assert(cacheables.isNotEmpty()) { "Should have Cacheable implementations from abstract class inheritance" }

    println("All tests passed! Including abstract class interface inheritance tests.")
}