plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-ksp")
}


dependencies {
    // 仅依赖 ktor-server-core，不引入 netty 等具体引擎
    implementation(libs.ktor.server.core)
}

