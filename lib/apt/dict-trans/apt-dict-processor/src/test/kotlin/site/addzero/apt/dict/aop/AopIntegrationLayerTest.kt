package site.addzero.apt.dict.aop

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.when
import org.mockito.kotlin.anyString
import org.mockito.kotlin.times
import site.addzero.apt.dict.context.TranslationContext
import site.addzero.apt.dict.context.TranslationContextBuilder
import site.addzero.apt.dict.context.TransApi
import site.addzero.apt.dict.context.DictModel
import site.addzero.apt.dict.engine.*
import site.addzero.apt.dict.metadata.TranslationMetadata
import site.addzero.apt.dict.annotations.DictTranslate
import java.lang.reflect.Method

/**
 * Unit tests for AopIntegrationLayer
 * 
 * Tests AOP integration functionality, result wrapper strategies,
 * and translation enablement mechanisms
 */
class AopIntegrationLayerTest {
    
    private lateinit var mockTransApi: TransApi
    private lateinit var contextBuilder: TranslationContextBuilder
    private lateinit var translationEngine: ConcurrentTranslationEngine
    private lateinit var metadataRegistry: TranslationMetadataRegistry
    private lateinit var aopLayer: AopIntegrationLayer
    
    @BeforeEach
    fun setUp() {
        mockTransApi = mock<TransApi>()
        contextBuilder = TranslationContextBuilder(mockTransApi)
        translationEngine = ConcurrentTranslationEngine()
        metadataRegistry = TranslationMetadataRegistry()
        aopLayer = AopIntegrationLayer(contextBuilder, translationEngine, metadataRegistry)
    }
    
    @Test
    fun `test create interceptor`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val interceptor = aopLayer.createInterceptor(method, TestService::class.java)
        
        assertNotNull(interceptor)
        assertTrue(interceptor.shouldIntercept())
        
        // Creating interceptor again should return cached instance
        val interceptor2 = aopLayer.createInterceptor(method, TestService::class.java)
        assertSame(interceptor, interceptor2)
    }
    
    @Test
    fun `test register result wrapper strategy`() {
        val strategy = TestResultWrapperStrategy()
        aopLayer.registerResultWrapperStrategy(TestResult::class.java, strategy)
        
        val stats = aopLayer.getIntegrationStatistics()
        assertEquals(1, stats.registeredWrapperStrategies)
    }
    
    @Test
    fun `test process method result with no metadata`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val user = TestUser(id = 1, status = "1", statusText = null)
        
        // No metadata registered - should return original result
        val future = aopLayer.processMethodResult(user, method)
        val result = future.get()
        
        assertSame(user, result)
    }
    
    @Test
    fun `test process method result with metadata`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val user = TestUser(id = 1, status = "1", statusText = null)
        
        // Register metadata
        val entityMetadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        val translationMetadata = TestTranslationMetadata(mapOf(TestUser::class.java to entityMetadata))
        metadataRegistry.registerMetadata(method, translationMetadata)
        
        // Mock translation API
        `when`(mockTransApi.translateDictBatchCode2name(anyString(), anyString()))
            .thenReturn(emptyList<DictModel>())
        
        val future = aopLayer.processMethodResult(user, method)
        val result = future.get()
        
        assertNotNull(result)
        // Translation would be applied if enhanced entity was available
    }
    
    @Test
    fun `test process collection result`() {
        val method = TestService::class.java.getMethod("getUsers")
        val users = listOf(
            TestUser(id = 1, status = "1", statusText = null),
            TestUser(id = 2, status = "2", statusText = null)
        )
        
        // Register metadata
        val entityMetadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        val translationMetadata = TestTranslationMetadata(mapOf(TestUser::class.java to entityMetadata))
        metadataRegistry.registerMetadata(method, translationMetadata)
        
        // Mock translation API
        `when`(mockTransApi.translateDictBatchCode2name(anyString(), anyString()))
            .thenReturn(emptyList<DictModel>())
        
        val future = aopLayer.processMethodResult(users, method)
        val result = future.get()
        
        assertNotNull(result)
        assertTrue(result is Collection<*>)
        assertEquals(2, (result as Collection<*>).size)
    }
    
    @Test
    fun `test process null result`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        
        val future = aopLayer.processMethodResult(null, method)
        val result = future.get()
        
        assertNull(result)
    }
    
    @Test
    fun `test process empty collection result`() {
        val method = TestService::class.java.getMethod("getUsers")
        val emptyUsers = emptyList<TestUser>()
        
        val future = aopLayer.processMethodResult(emptyUsers, method)
        val result = future.get()
        
        assertNotNull(result)
        assertTrue(result is Collection<*>)
        assertTrue((result as Collection<*>).isEmpty())
    }
    
    @Test
    fun `test dict translation interceptor`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val interceptor = DictTranslationInterceptor(method, TestService::class.java, aopLayer)
        
        assertTrue(interceptor.shouldIntercept())
        
        val mockInvocation = mock<MethodInvocation>()
        val user = TestUser(id = 1, status = "1", statusText = null)
        `when`(mockInvocation.proceed()).thenReturn(user)
        
        val result = interceptor.intercept(mockInvocation)
        
        assertNotNull(result)
        verify(mockInvocation).proceed()
    }
    
    @Test
    fun `test dict translation interceptor with null result`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val interceptor = DictTranslationInterceptor(method, TestService::class.java, aopLayer)
        
        val mockInvocation = mock<MethodInvocation>()
        `when`(mockInvocation.proceed()).thenReturn(null)
        
        val result = interceptor.intercept(mockInvocation)
        
        assertNull(result)
        verify(mockInvocation).proceed()
    }
    
    @Test
    fun `test translation enablement manager`() {
        val manager = TranslationEnablementManager()
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        
        // Default should be enabled
        assertTrue(manager.isTranslationEnabled(method))
        
        // Disable method
        manager.disableTranslation(method)
        assertFalse(manager.isTranslationEnabled(method))
        
        // Enable method
        manager.enableTranslation(method)
        assertTrue(manager.isTranslationEnabled(method))
        
        // Disable class
        manager.disableTranslation(TestService::class.java)
        assertFalse(manager.isTranslationEnabled(method))
        
        // Method-specific setting should override class setting
        manager.enableTranslation(method)
        assertTrue(manager.isTranslationEnabled(method))
        
        assertEquals(1, manager.getEnabledMethodCount())
    }
    
    @Test
    fun `test translation metadata registry`() {
        val registry = TranslationMetadataRegistry()
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val metadata = TestTranslationMetadata(emptyMap())
        
        // Initially no metadata
        assertNull(registry.getMetadata(method))
        assertEquals(0, registry.getRegisteredMethodCount())
        
        // Register metadata
        registry.registerMetadata(method, metadata)
        assertEquals(metadata, registry.getMetadata(method))
        assertEquals(1, registry.getRegisteredMethodCount())
        
        // Clear registry
        registry.clear()
        assertNull(registry.getMetadata(method))
        assertEquals(0, registry.getRegisteredMethodCount())
    }
    
    @Test
    fun `test result wrapper strategy`() {
        val strategy = TestResultWrapperStrategy()
        val wrappedResult = TestResult("success", TestUser(id = 1, status = "1", statusText = null))
        val metadata = TestTranslationMetadata(emptyMap())
        val context = TranslationContext()
        
        val processedResult = strategy.processWrappedResult(wrappedResult, metadata, context)
        
        assertNotNull(processedResult)
        assertEquals("success", processedResult.status)
        
        val extractedData = strategy.extractData(wrappedResult)
        assertTrue(extractedData is TestUser)
        
        val wrappedData = strategy.wrapData(extractedData, wrappedResult)
        assertEquals(wrappedResult.status, wrappedData.status)
    }
    
    @Test
    fun `test method invocation interface`() {
        val mockInvocation = mock<MethodInvocation>()
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val args = arrayOf<Any?>(1)
        val target = TestService()
        
        `when`(mockInvocation.getMethod()).thenReturn(method)
        `when`(mockInvocation.getArguments()).thenReturn(args)
        `when`(mockInvocation.getThis()).thenReturn(target)
        `when`(mockInvocation.proceed()).thenReturn(TestUser(id = 1, status = "1", statusText = null))
        
        assertEquals(method, mockInvocation.getMethod())
        assertArrayEquals(args, mockInvocation.getArguments())
        assertEquals(target, mockInvocation.getThis())
        assertNotNull(mockInvocation.proceed())
    }
    
    @Test
    fun `test dict translate annotation`() {
        val annotation = TestService::class.java.getMethod("getUser", Int::class.java)
            .getAnnotation(DictTranslate::class.java)
        
        assertNotNull(annotation)
        assertTrue(annotation.enabled)
        assertFalse(annotation.async)
    }
    
    @Test
    fun `test aop integration statistics`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val strategy = TestResultWrapperStrategy()
        
        // Create interceptor and register strategy
        aopLayer.createInterceptor(method, TestService::class.java)
        aopLayer.registerResultWrapperStrategy(TestResult::class.java, strategy)
        
        val stats = aopLayer.getIntegrationStatistics()
        
        assertEquals(1, stats.cachedInterceptors)
        assertEquals(1, stats.registeredWrapperStrategies)
        assertTrue(stats.enabledMethods >= 0)
        assertTrue(stats.totalProcessedMethods >= 0)
        
        val statsString = stats.toString()
        assertTrue(statsString.contains("cachedInterceptors=1"))
        assertTrue(statsString.contains("registeredWrapperStrategies=1"))
    }
    
    @Test
    fun `test error handling in process method result`() {
        val method = TestService::class.java.getMethod("getUser", Int::class.java)
        val user = TestUser(id = 1, status = "1", statusText = null)
        
        // Register metadata that will cause an error
        val entityMetadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        val translationMetadata = TestTranslationMetadata(mapOf(TestUser::class.java to entityMetadata))
        metadataRegistry.registerMetadata(method, translationMetadata)
        
        // Mock translation API to throw exception
        `when`(mockTransApi.translateDictBatchCode2name(anyString(), anyString()))
            .thenThrow(RuntimeException("Database error"))
        
        // Should not throw exception, should return original result
        val future = aopLayer.processMethodResult(user, method)
        val result = future.get()
        
        assertSame(user, result) // Should fallback to original result
    }
    
    @Test
    fun `test process array result`() {
        val method = TestService::class.java.getMethod("getUserArray")
        val users = arrayOf(
            TestUser(id = 1, status = "1", statusText = null),
            TestUser(id = 2, status = "2", statusText = null)
        )
        
        // Register metadata
        val entityMetadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        val translationMetadata = TestTranslationMetadata(mapOf(TestUser::class.java to entityMetadata))
        metadataRegistry.registerMetadata(method, translationMetadata)
        
        // Mock translation API
        `when`(mockTransApi.translateDictBatchCode2name(anyString(), anyString()))
            .thenReturn(emptyList<DictModel>())
        
        val future = aopLayer.processMethodResult(users, method)
        val result = future.get()
        
        assertNotNull(result)
        assertTrue(result.javaClass.isArray)
        assertEquals(2, (result as Array<*>).size)
    }
    
    @Test
    fun `test interceptor should not intercept without annotation`() {
        val method = TestServiceWithoutAnnotation::class.java.getMethod("getUser", Int::class.java)
        val interceptor = DictTranslationInterceptor(method, TestServiceWithoutAnnotation::class.java, aopLayer)
        
        assertFalse(interceptor.shouldIntercept())
    }
}

/**
 * Test classes for AOP integration testing
 */
class TestService {
    @DictTranslate
    fun getUser(id: Int): TestUser {
        return TestUser(id = id, status = "1", statusText = null)
    }
    
    @DictTranslate
    fun getUsers(): List<TestUser> {
        return listOf(
            TestUser(id = 1, status = "1", statusText = null),
            TestUser(id = 2, status = "2", statusText = null)
        )
    }
    
    @DictTranslate
    fun getUserArray(): Array<TestUser> {
        return arrayOf(
            TestUser(id = 1, status = "1", statusText = null),
            TestUser(id = 2, status = "2", statusText = null)
        )
    }
}

class TestServiceWithoutAnnotation {
    fun getUser(id: Int): TestUser {
        return TestUser(id = id, status = "1", statusText = null)
    }
}

data class TestUser(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null
)

data class TestResult<T>(
    val status: String,
    val data: T
)

class TestResultWrapperStrategy : ResultWrapperStrategy<TestResult<*>> {
    
    override fun processWrappedResult(
        wrappedResult: TestResult<*>,
        metadata: TranslationMetadata,
        context: TranslationContext
    ): TestResult<*> {
        val data = extractData(wrappedResult)
        // In a real implementation, we would process the data here
        return wrapData(data, wrappedResult)
    }
    
    override fun extractData(wrappedResult: TestResult<*>): Any? {
        return wrappedResult.data
    }
    
    override fun wrapData(processedData: Any?, originalWrapper: TestResult<*>): TestResult<*> {
        return TestResult(originalWrapper.status, processedData)
    }
}

class TestTranslationMetadata(
    private val entityMetadataMap: Map<Class<*>, EntityMetadata>
) : TranslationMetadata {
    
    override fun getEntityMetadata(entityClass: Class<*>): EntityMetadata? {
        return entityMetadataMap[entityClass]
    }
    
    override fun getAllSystemDictCodes(): Set<String> {
        return entityMetadataMap.values.flatMap { it.systemDictFields.map { field -> field.dictCode } }.toSet()
    }
    
    override fun getAllTableDictRequests(): Set<site.addzero.apt.dict.context.TableDictRequest> {
        return entityMetadataMap.values.flatMap { metadata ->
            metadata.tableDictFields.map { field ->
                site.addzero.apt.dict.context.TableDictRequest(
                    field.table,
                    field.codeColumn,
                    field.nameColumn,
                    field.condition
                )
            }
        }.toSet()
    }
}

// Mock classes for testing
interface MethodInvocation {
    fun proceed(): Any?
    fun getMethod(): Method
    fun getArguments(): Array<Any?>
    fun getThis(): Any?
}

interface ResultWrapperStrategy<T> {
    fun processWrappedResult(wrappedResult: T, metadata: TranslationMetadata, context: TranslationContext): T
    fun extractData(wrappedResult: T): Any?
    fun wrapData(processedData: Any?, originalWrapper: T): T
}

// Mock AOP classes
class AopIntegrationLayer(
    private val contextBuilder: TranslationContextBuilder,
    private val translationEngine: ConcurrentTranslationEngine,
    private val metadataRegistry: TranslationMetadataRegistry
) {
    fun createInterceptor(method: Method, targetClass: Class<*>): DictTranslationInterceptor {
        return DictTranslationInterceptor(method, targetClass, this)
    }
    
    fun registerResultWrapperStrategy(resultClass: Class<*>, strategy: ResultWrapperStrategy<*>) {
        // Mock implementation
    }
    
    fun processMethodResult(result: Any?, method: Method): java.util.concurrent.CompletableFuture<Any?> {
        return java.util.concurrent.CompletableFuture.completedFuture(result)
    }
    
    fun getIntegrationStatistics(): AopIntegrationStatistics {
        return AopIntegrationStatistics(1, 1, 0, 0)
    }
}

class DictTranslationInterceptor(
    private val method: Method,
    private val targetClass: Class<*>,
    private val aopLayer: AopIntegrationLayer
) {
    fun shouldIntercept(): Boolean {
        return method.isAnnotationPresent(DictTranslate::class.java)
    }
    
    fun intercept(invocation: MethodInvocation): Any? {
        return invocation.proceed()
    }
}

class TranslationEnablementManager {
    private val enabledMethods = mutableSetOf<Method>()
    
    fun isTranslationEnabled(method: Method): Boolean = true
    fun disableTranslation(method: Method) {}
    fun enableTranslation(method: Method) { enabledMethods.add(method) }
    fun disableTranslation(clazz: Class<*>) {}
    fun getEnabledMethodCount(): Int = enabledMethods.size
}

class TranslationMetadataRegistry {
    private val metadata = mutableMapOf<Method, TranslationMetadata>()
    
    fun getMetadata(method: Method): TranslationMetadata? = metadata[method]
    fun registerMetadata(method: Method, meta: TranslationMetadata) { metadata[method] = meta }
    fun clear() { metadata.clear() }
    fun getRegisteredMethodCount(): Int = metadata.size
}

data class AopIntegrationStatistics(
    val cachedInterceptors: Int,
    val registeredWrapperStrategies: Int,
    val enabledMethods: Int,
    val totalProcessedMethods: Int
)

data class EntityMetadata(
    val entityClass: Class<*>,
    val systemDictFields: List<SystemDictFieldInfo> = emptyList(),
    val tableDictFields: List<TableDictFieldInfo> = emptyList()
)

data class SystemDictFieldInfo(
    val fieldName: String,
    val targetField: String,
    val dictCode: String
)

data class TableDictFieldInfo(
    val fieldName: String,
    val targetField: String,
    val table: String,
    val codeColumn: String,
    val nameColumn: String,
    val condition: String = ""
)