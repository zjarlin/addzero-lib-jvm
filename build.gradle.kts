import java.text.SimpleDateFormat
import java.util.*

val versionDate: String? = SimpleDateFormat("yyyy.MM.dd").format(Date())
version = versionDate.toString()
// 或者使用setter方法
subprojects {
//    println("aaaaaaaaa$path")
    if (path.startsWith(":lib:")) {
//        println("aaaaaaa$name")
        apply(plugin = "site.addzero.publish-buddy")
    }
}
plugins {
    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
