# 为jimmer kotlin写的同构体生成插件
## 初衷是为了在mutiplatform shared共享模块中使用jimmer entity
## 或是为了多模块想把实体分享出去以便其他平台(三方)使用
```kotlin

//Add it in your settings.gradle.kts at the end of repositories:

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}


//Add it in your build.gradle.kts at the end of dependencies:

ksp("site.addzero:entity2iso-processor:+")





ksp {

    //同构体生成相关参数
    // 需要生成到目标模块的源代码目录
    val isomorphicOutputDir = project(":model")
        .extensions
        .getByType<SourceSetContainer>()["main"]
        .kotlin
        .srcDirs
        .first()
        .absolutePath
    //文件名已存在,则不生成(设为false每次都覆盖同构体)
    arg("skipExistsFiles", "false")
    //同构体输出目录
    arg("isomorphicOutputDir", isomorphicOutputDir)
    //同构体生成的包
    arg("isomorphicPackageName", "site.addzero.isomorphic")

    //同构体类名后缀
    arg("isomorphicClassSuffix", "Iso")

}

```
