package util

import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import getConfigContextByClass
import org.gradle.api.Project
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

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


