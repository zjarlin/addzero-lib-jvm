package site.addzero.buildlogic.publish

import site.addzero.gradle.PublishConventionExtension
import site.addzero.util.createExtension

val DEFAULT_PROJECT_DESCRIPTION = "addzero-kmp-scaffold"
val DEFAULT_AUTH_NAME = "zjarlin"
val DEFAULT_GIT_URL = "https://gitee.com/zjarlin/addzero.git"

// License 默认配置
val DEFAULT_LICENSE_NAME = "The Apache License, Version 2.0"
val DEFAULT_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"
val DEFAULT_LICENSE_DISTRIBUTION = "http://www.apache.org/licenses/LICENSE-2.0.txt"


val creatdaseExtension = createExtension<PublishConventionExtension>().apply {
    projectDescription.set(DEFAULT_PROJECT_DESCRIPTION)
    authorName.set(DEFAULT_AUTH_NAME)
    gitUrl.set(DEFAULT_GIT_URL)
    emailDomain.set("outlook.com")

    // License 默认值
    licenseName.set(DEFAULT_LICENSE_NAME)
    licenseUrl.set(DEFAULT_LICENSE_URL)
    licenseDistribution.set(DEFAULT_LICENSE_DISTRIBUTION)

}
