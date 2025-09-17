package site.addzero.gradle.tool

import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.gradle.api.Project
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object KonfigUtil {
    fun getProjectProperties(project: Project, envFile: String = ".env.prod"): Map<String, String> {

        val propertiesFile = File("${project.rootDir}/$envFile")
        val properties = Properties()

        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use {
                properties.load(it)
            }
        }

        return properties.entries.associate { it.key.toString() to it.value.toString() }
    }
}

fun TargetConfigDsl.defByMap(map: Map<String, Any?>) {
    map.forEach {
        val triple = Triple(it.key, it.value, String::class.createType())
        defByTriple(triple)

    }
}

fun <T : Any> getConfigContextByClass(klass: KClass<out T>): List<Triple<String, Any?, KType>> {
    val instance = klass.objectInstance!!
    val memberProperties = klass.memberProperties
    return memberProperties.map { property ->
        property.isAccessible = true
        val name = property.name
        val value = try {
            // 首先尝试无参调用（适用于字段）
            property.getter.call()
        } catch (e: IllegalArgumentException) {
            // 如果失败，尝试带实例调用（适用于实例属性）
            property.getter.call(instance)
        }
        val type = property.returnType
        Triple(name, value, type)
    }
}


fun TargetConfigDsl.defByClass(klass: KClass<*>) {
    val configContextByClass = getConfigContextByClass(klass)
    configContextByClass.forEach {
        defByTriple(it)
    }

}

fun TargetConfigDsl.defByTriple(it: Triple<String, Any?, KType>) {
    val name = it.first
    val value = it.second
    val ktType = it.third.toString()
    when {
        ktType.contains("String") -> {
            buildConfigField(FieldSpec.Type.STRING, name, value.toString())
        }

        ktType.contains("Int") -> {
            buildConfigField(FieldSpec.Type.INT, name, value.toString())
        }

        ktType.contains("Float") -> {
            buildConfigField(FieldSpec.Type.FLOAT, name, "${value}f")
        }

        ktType.contains("Long") -> {
            buildConfigField(FieldSpec.Type.LONG, name, "${value}L")
        }

        ktType.contains("Boolean") -> {
            buildConfigField(FieldSpec.Type.BOOLEAN, name, value.toString().lowercase())
        }

        else -> {
            // 处理未知类型，可根据需要添加日志或抛出异常
            println("Unsupported type: $ktType for field $name")
        }
    }
}


