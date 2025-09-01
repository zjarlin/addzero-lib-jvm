plugins {
    id("com.google.devtools.ksp")
}
ksp {
    arg("controller2LazyhttpPkg", "com.addzero.generated.api")
    // 字典表配置（小驼峰命名）


}
