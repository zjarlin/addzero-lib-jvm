package site.addzero.easyexcel

import cn.idev.excel.EasyExcel
import cn.idev.excel.annotation.ExcelProperty
import cn.idev.excel.converters.AutoConverter
import cn.idev.excel.converters.Converter
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.FieldAccessor
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.jvmName


@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class ExcelPropertyDelegate(
    val value: Array<String> = [""],
    val index: Int = -1,
    val order: Int = Int.MAX_VALUE,
    val converter: KClass<out Converter<*>> = AutoConverter::class
)

class DynamicExcelClassGenerator {

    private val cache = ConcurrentHashMap<KClass<*>, KClass<*>>()

    fun getGeneratedClass(originalClass: KClass<*>): KClass<*> {
        return cache.computeIfAbsent(originalClass) {
            generateClass(it)
        }
    }


    private fun generateClass(originalClass: KClass<*>): KClass<*> {
        val originalClassName = originalClass.jvmName
        val generatedClassName = originalClassName + "Generated"

        var builder = ByteBuddy().subclass(Any::class.java).name(generatedClassName)

        originalClass.declaredMemberProperties.forEach { prop ->
            val delegateAnnotation = prop.javaGetter?.getAnnotation(ExcelPropertyDelegate::class.java)

            if (delegateAnnotation != null) {
                val annotationBuilder = AnnotationDescription.Builder.ofType(ExcelProperty::class.java)
                    .defineArray("value", *delegateAnnotation.value)
                    .define("index", delegateAnnotation.index)
                    .define("order", delegateAnnotation.order)
                    .define(
                        "converter",
                        TypeDescription.ForLoadedType.of(delegateAnnotation.converter.java)
                    )

                val fieldType = (prop.returnType.classifier as? KClass<*>)?.java ?: Any::class.java

                builder = builder.defineField(prop.name, fieldType, Visibility.PRIVATE)
                    .annotateField(annotationBuilder.build())
                    .defineMethod("get${prop.name.replaceFirstChar(Char::titlecase)}", fieldType, Visibility.PUBLIC)
                    .intercept(FieldAccessor.ofField(prop.name))
                    .defineMethod("set${prop.name.replaceFirstChar(Char::titlecase)}", Void.TYPE, Visibility.PUBLIC)
                    .withParameters(fieldType)
                    .intercept(FieldAccessor.ofField(prop.name).setsArgumentAt(0))

            }
        }

        val generatedClass = builder.make()
            .load(originalClass.java.classLoader, ClassLoadingStrategy.Default.WRAPPER)
            .loaded

        return generatedClass.kotlin
    }
}

fun main() {
    data class TestBean(
        @get:ExcelPropertyDelegate(value = ["产品编号"], index = 0)
        var productNo: String? = null) {
        constructor() : this(null)
    }

    val clazz = DynamicExcelClassGenerator().getGeneratedClass(TestBean::class).java
    val file = File("./test.xlsx").absolutePath
    EasyExcel.write(file)
        .head(clazz)
        .sheet()
        .doWrite(
            listOf(
                TestBean(
                    productNo = "test",
                )
            )
        )
    val mapper = jsonMapper().registerKotlinModule()
    val products = EasyExcel.read(file)
        .head(clazz)
        .sheet()
        .doReadSync<Any>()
        .map {
            mapper.treeToValue<TestBean>(mapper.valueToTree(it))
        }
    println(products)
}
