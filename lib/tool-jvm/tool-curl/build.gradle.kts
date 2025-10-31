plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    application
//    id("kmp-json")
}

dependencies {
    // JSON
//    implementation("org.json:json:20231013")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // 测试依赖
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

application {
    mainClass.set("site.addzero.util.CurlExecutionMainKt")
}