package site.addzero.gradle.plugin

enum class RepoType(val urlTemplate: String) {
    GITEE("https://gitee.com/%s/%s.git"),
    GITHUB("https://github.com/%s/%s.git")
}
