/**
 * KCloud Core - 核心业务模块
 *
 * 包含数据库、同步引擎、状态管理等通用业务逻辑
 * 被 kcloud 桌面端和 kcloud-server 共享
 */
plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 协程
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
            // 时间处理
            implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-datetime").get())
            // 日志
            implementation(libs.findLibrary("kotlin-logging").get())
            implementation(project(":lib:spec:system-spec"))
        }

        jvmMain.dependencies {
            implementation(project(":lib:kcloud-paths"))
            // SQLite JDBC驱动
            implementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
            // HikariCP连接池
            implementation(libs.findLibrary("hikaricp").get())
            // SSHJ - SSH/SFTP 客户端
            implementation(libs.findLibrary("com-hierynomus-sshj").get())
            // JmDNS - mDNS 服务发现 (P2P)
            implementation(libs.findLibrary("jmdns").get())
        }
    }
}
