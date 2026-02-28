package site.addzero.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property

//typealias PjPredicate = (Project)->Boolean
interface PublishConventionExtension {
    val projectDescription: Property<String>
    val authorName: Property<String>
    val gitUrl: Property<String>
    val emailDomain: Property<String>
    val enableAggregatePublishTasksByParentDir: Property<Boolean>

    // License 配置
    val licenseName: Property<String>
    val licenseUrl: Property<String>

    val licenseDistribution: Property<String>

//    var predicateSubProject: Property<PjPredicate>
}





