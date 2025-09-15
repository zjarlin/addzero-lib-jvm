import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun KotlinMultiplatformExtension.doIos(nt: List<KotlinNativeTarget>) {
    nt.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

}


fun KotlinMultiplatformExtension.defIos(): List<KotlinNativeTarget> {
    val listOf = listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )
    return listOf

}

//fun TargetConfigDsl.definePro(
//    name: String,
//    value: String,
//    onDo: (name: String, value: String) -> Unit
//) {
//    onDo(name, value)
//}

//fun BuildKonfigExtension.devContext(
//    name: String,
//    value: String,
//    type: String,
//    env:String,
//    onDo: TargetConfigDsl.() -> Unit
//) {
//    defaultConfigs(env) {
//        onDo()
//    }
//}

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


