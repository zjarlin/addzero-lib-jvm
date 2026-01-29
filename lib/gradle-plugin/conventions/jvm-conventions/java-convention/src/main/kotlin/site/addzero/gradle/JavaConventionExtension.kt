package site.addzero.gradle
import org.gradle.api.provider.Property

/**
 * Extension for configuring Java convention plugin settings
 */
abstract class JavaConventionExtension {
    /**
     * JDK version to use for compilation and toolchain
     * Default: "8"
     */
    abstract val jdkVersion: Property<String>

    /**
     * JUnit Jupiter version for testing
     * Default: "5.8.1"
     */
    abstract val junitVersion: Property<String>

    /**
     * Lombok version for annotation processing
     * Default: "1.18.24"
     */
    abstract val lombokVersion: Property<String>

    init {
        jdkVersion.convention("8")
        junitVersion.convention("5.8.1")
        lombokVersion.convention("1.18.24")
    }
}