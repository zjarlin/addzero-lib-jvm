listOf(
    project(":app"),
    project(":lib:tool-jvm:tool-bean"),
    project(":lib:tool-starter:dict-trans-spring-boot-starter"),
).forEach { targetProject ->
    targetProject.configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("site.addzero:tool-reflection"))
                .using(project(":lib:tool-jvm:tool-reflection"))
                .because("示例需要验证当前工作区的 ImprovedReflectUtil，而不是已发布旧包")
            substitute(module("site.addzero:dict-trans-core"))
                .using(project(":lib:apt:dict-trans:apt-dict-trans-core"))
                .because("示例需要验证当前工作区支持 PROPERTY_GETTER/RUNTIME 的 @Dict")
        }
    }
}
