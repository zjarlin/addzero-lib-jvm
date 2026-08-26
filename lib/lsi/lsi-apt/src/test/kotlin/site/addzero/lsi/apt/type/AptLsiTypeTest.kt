package site.addzero.lsi.apt.type

import java.net.URI
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AptLsiTypeTest {

    @Test
    fun `keeps annotations out of declared type names`() {
        val processor = TypeCaptureProcessor()
        val compiler = ToolProvider.getSystemJavaCompiler()
        val sources = listOf(
            source(
                "javax.validation.constraints.Size",
                """
                    package javax.validation.constraints;
                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Target;
                    @Target({ElementType.METHOD, ElementType.TYPE_USE})
                    public @interface Size { int max() default Integer.MAX_VALUE; }
                """.trimIndent(),
            ),
            source(
                "javax.validation.constraints.Pattern",
                """
                    package javax.validation.constraints;
                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Target;
                    @Target({ElementType.METHOD, ElementType.TYPE_USE})
                    public @interface Pattern { String regexp(); }
                """.trimIndent(),
            ),
            source(
                "org.babyfish.jimmer.sql.Column",
                """
                    package org.babyfish.jimmer.sql;
                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Target;
                    @Target({ElementType.METHOD, ElementType.TYPE_USE})
                    public @interface Column { String sqlType() default ""; }
                """.trimIndent(),
            ),
            source(
                "demo.CallAlgRecord",
                """
                    package demo;
                    import javax.validation.constraints.Pattern;
                    import javax.validation.constraints.Size;
                    import org.babyfish.jimmer.sql.Column;
                    public interface CallAlgRecord {
                        @Size(max = 50)
                        @Pattern(regexp = "[^\\d]+\\S+")
                        @Column(sqlType = "varchar")
                        String id();
                    }
                """.trimIndent(),
            ),
        )

        val task = compiler.getTask(null, null, null, listOf("-proc:only"), null, sources)
        task.setProcessors(listOf(processor))

        assertTrue(task.call())
        assertEquals("String", processor.simpleName)
        assertEquals("java.lang.String", processor.qualifiedName)
        assertEquals("java.lang.String", processor.returnTypeName)
    }

    private class TypeCaptureProcessor : javax.annotation.processing.AbstractProcessor() {
        var simpleName: String? = null
        var qualifiedName: String? = null
        var returnTypeName: String? = null

        override fun getSupportedAnnotationTypes(): Set<String> = setOf("*")

        override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

        override fun process(
            annotations: Set<TypeElement>,
            roundEnv: javax.annotation.processing.RoundEnvironment,
        ): Boolean {
            val type = processingEnv.elementUtils.getTypeElement("demo.CallAlgRecord") ?: return false
            val method = type.enclosedElements.single { it.simpleName.contentEquals("id") }
                as javax.lang.model.element.ExecutableElement
            val lsiMethod = site.addzero.lsi.apt.method.AptLsiMethod(processingEnv.elementUtils, method)
            simpleName = lsiMethod.returnType?.simpleName
            qualifiedName = lsiMethod.returnType?.qualifiedName
            returnTypeName = lsiMethod.returnTypeName
            return false
        }
    }

    private fun source(qualifiedName: String, content: String): JavaFileObject =
        object : SimpleJavaFileObject(
            URI.create("string:///${qualifiedName.replace('.', '/')}.java"),
            JavaFileObject.Kind.SOURCE,
        ) {
            override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = content
        }
}
