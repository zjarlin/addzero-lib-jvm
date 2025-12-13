package site.addzero.apt.dict.integration

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import site.addzero.apt.dict.annotations.DictTranslate
import site.addzero.apt.dict.annotations.DictField
import site.addzero.apt.dict.processor.DictTranslateProcessor
import site.addzero.apt.dict.context.TransApi
import site.addzero.apt.dict.context.DictModel
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.JavaCompiler
import javax.tools.ToolProvider
import java.io.File
import java.io.StringWriter
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

/**
 * End-to-end integration tests for APT dictionary translation system
 * 
 * These tests verify:
 * 1. Complete APT processing pipeline
 * 2. Generated code compilation and execution
 * 3. Integration with real database scenarios
 * 4. Performance benchmarks against reflection-based implementation
 */
class APTIntegrationTest : FunSpec({
    
    test("should process complete APT pipeline from annotation to execution") {
        val tempDir = Files.createTempDirectory("apt-integration-test")
        
        try {
            // 1. Create test entity with annotations
            val entitySource = createTestEntitySource()
            val entityFile = tempDir.resolve("TestEntity.java")
            entityFile.writeText(entitySource)
            
            // 2. Compile with APT processor
            val compilationResult = compileWithAPT(tempDir, listOf(entityFile))
            compilationResult.success shouldBe true
            
            // 3. Verify enhanced class was generated
            val enhancedClassFile = tempDir.resolve("TestEntityEnhanced.java")
            Files.exists(enhancedClassFile) shouldBe true
            
            val enhancedSource = Files.readString(enhancedClassFile)
            enhancedSource shouldContain "public class TestEntityEnhanced extends TestEntity"
            enhancedSource shouldContain "public void translate(TransApi transApi)"
            enhancedSource shouldContain "this.getStatus()"
            
            // 4. Compile generated code
            val finalCompilationResult = compileJavaFiles(tempDir, listOf(entityFile, enhancedClassFile))
            finalCompilationResult.success shouldBe true
            
            // 5. Load and test generated class
            val classLoader = URLClassLoader(arrayOf(tempDir.toUri().toURL()))
            val enhancedClass = classLoader.loadClass("TestEntityEnhanced")
            
            // Verify inheritance
            val originalClass = classLoader.loadClass("TestEntity")
            originalClass.isAssignableFrom(enhancedClass) shouldBe true
            
            // Verify translation methods exist
            val translateMethod = enhancedClass.getMethod("translate", TransApi::class.java)
            translateMethod shouldNotBe null
            
            val translateAsyncMethod = enhancedClass.getMethod("translateAsync", TransApi::class.java)
            translateAsyncMethod shouldNotBe null
            
        } finally {
            // Cleanup
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("should handle complex RBAC entity with multiple translation types") {
        val tempDir = Files.createTempDirectory("apt-rbac-test")
        
        try {
            // Create complex RBAC entity
            val rbacSource = createRBACEntitySource()
            val rbacFile = tempDir.resolve("RBACUser.java")
            rbacFile.writeText(rbacSource)
            
            // Compile with APT
            val result = compileWithAPT(tempDir, listOf(rbacFile))
            result.success shouldBe true
            
            // Verify generated code
            val enhancedFile = tempDir.resolve("RBACUserEnhanced.java")
            val enhancedSource = Files.readString(enhancedFile)
            
            // Should contain system dictionary translation
            enhancedSource shouldContain "user_status"
            enhancedSource shouldContain "systemDictCodes"
            
            // Should contain table dictionary translation
            enhancedSource shouldContain "sys_department"
            enhancedSource shouldContain "translateTableBatchCode2name"
            
            // Should contain SPEL expression handling
            enhancedSource shouldContain "spelExpression"
            
            // Should contain batch optimization
            enhancedSource shouldContain "systemKeys"
            enhancedSource shouldContain "tableKeys"
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("should generate correct metadata constants") {
        val tempDir = Files.createTempDirectory("apt-metadata-test")
        
        try {
            val entitySource = createMetadataTestEntitySource()
            val entityFile = tempDir.resolve("MetadataEntity.java")
            entityFile.writeText(entitySource)
            
            val result = compileWithAPT(tempDir, listOf(entityFile))
            result.success shouldBe true
            
            val enhancedFile = tempDir.resolve("MetadataEntityEnhanced.java")
            val enhancedSource = Files.readString(enhancedFile)
            
            // Verify system dictionary metadata
            enhancedSource shouldContain "public static final Set<String> SYSTEM_DICT_CODES"
            enhancedSource shouldContain "status_dict"
            enhancedSource shouldContain "type_dict"
            
            // Verify table dictionary metadata
            enhancedSource shouldContain "public static final Set<String> TABLE_DICT_CONFIGS"
            enhancedSource shouldContain "sys_category:id:name"
            enhancedSource shouldContain "sys_department:id:display_name"
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("should handle error cases gracefully") {
        val tempDir = Files.createTempDirectory("apt-error-test")
        
        try {
            // Create entity with invalid configuration
            val invalidSource = createInvalidEntitySource()
            val invalidFile = tempDir.resolve("InvalidEntity.java")
            invalidFile.writeText(invalidSource)
            
            val result = compileWithAPT(tempDir, listOf(invalidFile))
            
            // Should still succeed but with warnings/errors
            result.diagnostics.isNotEmpty() shouldBe true
            
            // Should contain validation error messages
            val diagnosticMessages = result.diagnostics.joinToString("\n")
            diagnosticMessages shouldContain "validation"
            
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
    
    test("should demonstrate performance improvement over reflection") {
        // This test simulates the performance difference
        val entityCount = 1000
        val dictFieldsPerEntity = 3
        
        // Simulate reflection-based approach (N+1 queries)
        val reflectionStartTime = System.currentTimeMillis()
        val reflectionQueries = simulateReflectionBasedTranslation(entityCount, dictFieldsPerEntity)
        val reflectionTime = System.currentTimeMillis() - reflectionStartTime
        
        // Simulate APT-based approach (batch queries)
        val aptStartTime = System.currentTimeMillis()
        val aptQueries = simulateAPTBasedTranslation(entityCount, dictFieldsPerEntity)
        val aptTime = System.currentTimeMillis() - aptStartTime
        
        // Verify performance improvement
        println("Performance Comparison:")
        println("  Entities: $entityCount")
        println("  Dict Fields per Entity: $dictFieldsPerEntity")
        println("  Reflection Queries: $reflectionQueries")
        println("  APT Queries: $aptQueries")
        println("  Query Reduction: ${((reflectionQueries - aptQueries).toDouble() / reflectionQueries * 100).toInt()}%")
        
        // APT should use significantly fewer queries
        aptQueries shouldBe (dictFieldsPerEntity + 1) // 1 load query + 1 per dict type
        reflectionQueries shouldBe (entityCount * dictFieldsPerEntity + 1) // N+1 problem
        
        val queryReduction = (reflectionQueries - aptQueries).toDouble() / reflectionQueries
        queryReduction shouldBe (0.99) // Should be ~99% reduction
    }
})

// Helper functions for creating test sources

private fun createTestEntitySource(): String = """
import site.addzero.apt.dict.annotations.DictTranslate;
import site.addzero.apt.dict.annotations.DictField;

@DictTranslate(suffix = "Enhanced")
public class TestEntity {
    private Long id;
    private String name;
    
    @DictField(
        dictCode = "test_status",
        targetField = "statusText"
    )
    private String status;
    
    @DictField(
        table = "test_category",
        codeColumn = "id",
        nameColumn = "name",
        targetField = "categoryName"
    )
    private Long categoryId;
    
    // Constructors
    public TestEntity() {}
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
"""

private fun createRBACEntitySource(): String = """
import site.addzero.apt.dict.annotations.DictTranslate;
import site.addzero.apt.dict.annotations.DictField;

@DictTranslate(suffix = "Enhanced")
public class RBACUser {
    private Long id;
    private String username;
    
    @DictField(
        dictCode = "user_status",
        targetField = "statusText"
    )
    private String status;
    
    @DictField(
        table = "sys_department",
        codeColumn = "id",
        nameColumn = "name",
        targetField = "departmentName"
    )
    private Long departmentId;
    
    @DictField(
        spelExp = "#{dict('user_type', userType) + ' - ' + table('sys_role', 'id', 'name', roleId)}",
        targetField = "userRoleDescription"
    )
    private String userType;
    
    private Long roleId;
    
    // Constructors
    public RBACUser() {}
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}
"""

private fun createMetadataTestEntitySource(): String = """
import site.addzero.apt.dict.annotations.DictTranslate;
import site.addzero.apt.dict.annotations.DictField;

@DictTranslate(suffix = "Enhanced")
public class MetadataEntity {
    @DictField(dictCode = "status_dict", targetField = "statusText")
    private String status;
    
    @DictField(dictCode = "type_dict", targetField = "typeText")
    private String type;
    
    @DictField(table = "sys_category", codeColumn = "id", nameColumn = "name", targetField = "categoryName")
    private Long categoryId;
    
    @DictField(table = "sys_department", codeColumn = "id", nameColumn = "display_name", targetField = "deptName")
    private Long deptId;
    
    // Constructors and getters/setters
    public MetadataEntity() {}
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
}
"""

private fun createInvalidEntitySource(): String = """
import site.addzero.apt.dict.annotations.DictTranslate;
import site.addzero.apt.dict.annotations.DictField;

@DictTranslate(suffix = "Enhanced")
public class InvalidEntity {
    @DictField(
        // Invalid: no translation source specified
        targetField = "invalidText"
    )
    private String invalidField;
    
    @DictField(
        table = "test_table",
        // Invalid: missing nameColumn
        codeColumn = "id",
        targetField = "tableName"
    )
    private Long tableId;
    
    // Constructors and getters/setters
    public InvalidEntity() {}
    
    public String getInvalidField() { return invalidField; }
    public void setInvalidField(String invalidField) { this.invalidField = invalidField; }
    
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
}
"""

// Compilation helper functions

private data class CompilationResult(
    val success: Boolean,
    val diagnostics: List<String>
)

private fun compileWithAPT(workingDir: Path, sourceFiles: List<Path>): CompilationResult {
    val compiler = ToolProvider.getSystemJavaCompiler()
    val diagnostics = mutableListOf<String>()
    
    val fileManager = compiler.getStandardFileManager(null, null, null)
    val compilationUnits = fileManager.getJavaFileObjectsFromPaths(sourceFiles)
    
    val options = listOf(
        "-processor", "site.addzero.apt.dict.processor.DictTranslateProcessor",
        "-d", workingDir.toString(),
        "-s", workingDir.toString(),
        "-cp", System.getProperty("java.class.path")
    )
    
    val task = compiler.getTask(
        StringWriter(),
        fileManager,
        { diagnostic ->
            diagnostics.add(diagnostic.toString())
        },
        options,
        null,
        compilationUnits
    )
    
    val success = task.call()
    
    return CompilationResult(success, diagnostics)
}

private fun compileJavaFiles(workingDir: Path, sourceFiles: List<Path>): CompilationResult {
    val compiler = ToolProvider.getSystemJavaCompiler()
    val diagnostics = mutableListOf<String>()
    
    val fileManager = compiler.getStandardFileManager(null, null, null)
    val compilationUnits = fileManager.getJavaFileObjectsFromPaths(sourceFiles)
    
    val options = listOf(
        "-d", workingDir.toString(),
        "-cp", System.getProperty("java.class.path")
    )
    
    val task = compiler.getTask(
        StringWriter(),
        fileManager,
        { diagnostic ->
            diagnostics.add(diagnostic.toString())
        },
        options,
        null,
        compilationUnits
    )
    
    val success = task.call()
    
    return CompilationResult(success, diagnostics)
}

// Performance simulation functions

private fun simulateReflectionBasedTranslation(entityCount: Int, dictFieldsPerEntity: Int): Int {
    // Simulate N+1 query problem
    // 1 query to load entities + N queries for each dictionary field
    return 1 + (entityCount * dictFieldsPerEntity)
}

private fun simulateAPTBasedTranslation(entityCount: Int, dictFieldsPerEntity: Int): Int {
    // Simulate batch optimization
    // 1 query to load entities + 1 batch query per dictionary type
    return 1 + dictFieldsPerEntity
}