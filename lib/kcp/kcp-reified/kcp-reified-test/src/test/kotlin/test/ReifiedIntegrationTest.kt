package test

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertNotNull
import site.addzero.kcp.annotations.GenerateReified

/**
 * 集成测试 - 验证生成的 reified 方法
 *
 * 注意：这是实际编译后执行的测试，验证生成的代码能否正常工作
 */

// 测试用的基础类型
interface Entity
interface View<E : Entity>

// 测试容器 - 插件应该为这个类生成 reified 方法
@GenerateReified
interface TestViewContainer<E : Entity> {
    // 原始方法
    fun <V : View<E>> findById(id: String, clazz: KClass<V>): V?
    fun <V : View<E>> findByIds(ids: List<String>, clazz: KClass<V>): List<V>
    fun <V : View<E>> findByType(id: String, type: KClass<V>): V?

    // 不应该生成的方法（参数名不符合约定）
    fun <V : View<E>> findWithClass(id: String, klass: KClass<V>): V?
}

// 实现测试容器
class TestViewContainerImpl<E : Entity> : TestViewContainer<E> {
    private val views = mutableMapOf<String, View<E>>()

    override fun <V : View<E>> findById(id: String, clazz: KClass<V>): V? {
        @Suppress("UNCHECKED_CAST")
        return views[id] as? V
    }

    override fun <V : View<E>> findByIds(ids: List<String>, clazz: KClass<V>): List<V> {
        @Suppress("UNCHECKED_CAST")
        return ids.mapNotNull { views[it] as? V }
    }

    override fun <V : View<E>> findByType(id: String, type: KClass<V>): V? {
        @Suppress("UNCHECKED_CAST")
        return views[id] as? V
    }

    override fun <V : View<E>> findWithClass(id: String, klass: KClass<V>): V? {
        @Suppress("UNCHECKED_CAST")
        return views[id] as? V
    }
}

// 具体实现类
data class TestEntity(val id: String) : Entity
class TestView(val entity: TestEntity) : View<TestEntity>

/**
 * 集成测试
 *
 * 这个测试验证：
 * 1. 插件生成的 reified 方法能够被调用
 * 2. 生成的代码正确地转发到原始方法
 */
class ReifiedIntegrationTest {

    @Test
    fun testFindByIdGenerated() {
        val container = TestViewContainerImpl<TestEntity>()
        val view = TestView(TestEntity("123"))
        val id = "test-id"

        // If the FIR plugin works, this call should compile.
        val generatedResult: TestView? = container.findView<TestView>(id)
        assert(generatedResult == null) { "Expected null for non-existent view" }

        // Also verify the overload generated from findByIds.
        val generatedList: List<TestView> = container.findView<TestView>(listOf("a", "b"))
        assert(generatedList.isEmpty()) { "Expected empty list for non-existent views" }

        // Verify original method still works.
        val result = container.findById(id, TestView::class)
        // result should be null because we didn't add the view
        assert(result == null) { "Expected null for non-existent view" }
    }

    @Test
    fun testOriginalMethodWorks() {
        val container = TestViewContainerImpl<TestEntity>()

        // 测试原始方法
        val result1: TestView? = container.findById("test", TestView::class)
        assert(result1 == null)

        val result2: List<TestView> = container.findByIds(listOf("a", "b"), TestView::class)
        assert(result2.isEmpty())

        val result3: TestView? = container.findByType("test", TestView::class)
        assert(result3 == null)
    }
}

/**
 * 测试辅助说明
 *
 * 要验证 KCP 插件是否正常工作，有以下几种方法：
 *
 * === 方法 1: 检查编译后的字节码 ===
 * 1. 编译测试项目: ./gradlew build
 * 2. 使用 javap 反编译: javap -c -v TestViewContainer.class
 * 3. 查找名为 "findView" 的方法
 * 4. 检查方法签名是否包含 "reified"
 *
 * === 方法 2: 启用编译器日志 ===
 * 1. 在 build.gradle.kts 中启用插件日志
 * 2. 编译时查看控制台输出
 * 3. 应该看到 "[ReifiedPlugin] Processing..." 的日志
 *
 * === 方法 3: IDE 代码提示 ===
 * 1. 在 IDE 中打开 TestViewContainer
 * 2. 尝试调用 container.findView<TestView>(id)
 * 3. 如果 IDE 自动补全显示该方法，说明插件生效
 *
 * === 方法 4: 单元测试 + IR Dump ===
 * 使用 Kotlin 编译器测试框架，导出 IR 并检查生成的函数
 */
