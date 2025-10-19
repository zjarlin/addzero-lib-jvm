package site.addzero.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property


interface PublishConventionExtension {
    val projectDescription: Property<String>
    val authorName: Property<String>
    val gitUrl: Property<String>
    val emailDomain: Property<String>

    // License 配置
    val licenseName: Property<String>
    val licenseUrl: Property<String>
    val licenseDistribution: Property<String>

    var predicateSubProject: (Project)->Boolean
}





