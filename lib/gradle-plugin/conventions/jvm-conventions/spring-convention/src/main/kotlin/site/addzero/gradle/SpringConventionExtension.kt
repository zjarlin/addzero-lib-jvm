package site.addzero.gradle

import javax.inject.Inject

open class SpringConventionExtension @Inject constructor() {

    /**
     * Spring Boot version
     * Default: 3.2.0
     */
    var springBootVersion: String = "3.2.0"

    /**
     * Whether to include spring-boot-starter-test
     * Default: true
     */
    var includeStarterTest: Boolean = true

    /**
     * Whether to include H2 database for testing
     * Default: true
     */
    var includeH2: Boolean = true

    /**
     * Whether to include spring-boot-starter-web in test implementation
     * Default: true
     */
    var includeWebInTest: Boolean = true

    /**
     * Whether to include spring-boot-configuration-processor
     * Default: true
     */
    var includeConfigurationProcessor: Boolean = true

    /**
     * Whether to include spring-boot-autoconfigure as compile only
     * Default: true
     */
    var includeAutoConfigure: Boolean = true

    /**
     * Additional dependencies to include
     */
    val additionalDependencies = mutableMapOf<String, String>()

    /**
     * Additional test dependencies to include
     */
    val additionalTestDependencies = mutableMapOf<String, String>()

    /**
     * Additional annotation processors to include
     */
    val additionalAnnotationProcessors = mutableMapOf<String, String>()

    /**
     * Additional compile-only dependencies to include
     */
    val additionalCompileOnlyDependencies = mutableMapOf<String, String>()

    /**
     * Add additional implementation dependency
     */
    fun implementation(dependency: String) {
        additionalDependencies[dependency] = "implementation"
    }

    /**
     * Add additional test implementation dependency
     */
    fun testImplementation(dependency: String) {
        additionalTestDependencies[dependency] = "testImplementation"
    }

    /**
     * Add additional annotation processor
     */
    fun annotationProcessor(dependency: String) {
        additionalAnnotationProcessors[dependency] = "annotationProcessor"
    }

    /**
     * Add additional compile-only dependency
     */
    fun compileOnly(dependency: String) {
        additionalCompileOnlyDependencies[dependency] = "compileOnly"
    }
}