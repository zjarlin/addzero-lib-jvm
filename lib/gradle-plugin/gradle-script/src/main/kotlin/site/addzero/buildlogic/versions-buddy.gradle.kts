import site.addzero.gradle.DomainConventionExtension
import site.addzero.util.createExtension
import java.text.SimpleDateFormat
import java.util.*

// 定义默认域名

val create = createExtension<DomainConventionExtension>().apply {
    domain.convention("site.addzero")
}



val versionDate: String? = SimpleDateFormat("yyyy.MM.dd").format(Date())
val rootName = rootDir.name
val name1 = projectDir.name

allprojects {
    group = "${create.domain.get()}.${name1}"
    version = versionDate.toString()
}
