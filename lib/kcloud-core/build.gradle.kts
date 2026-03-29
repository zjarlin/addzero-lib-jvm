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

kotlin {
    sourceSets {
        commonMain.dependencies {
            // 协程
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            // 时间处理
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            // 日志
            implementation("io.github.oshai:kotlin-logging:7.0.3")
            implementation(project(":lib:spec:system-spec"))
        }

        jvmMain.dependencies {
            implementation(project(":lib:kcloud-paths"))
            // SQLite JDBC驱动
            implementation("org.xerial:sqlite-jdbc:3.45.1.0")
            // HikariCP连接池
            implementation("com.zaxxer:HikariCP:5.1.0")
            // SSHJ - SSH/SFTP 客户端
            implementation("com.hierynomus:sshj:0.38.0")
            // JmDNS - mDNS 服务发现 (P2P)
            implementation("org.jmdns:jmdns:3.5.9")
        }
    }
}
