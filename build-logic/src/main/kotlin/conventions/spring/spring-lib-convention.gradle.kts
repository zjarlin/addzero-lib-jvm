plugins {
    id("spring-common")
    id("org.springframework.boot") apply false
}

// 在插件应用之后配置任务
afterEvaluate {
    // 禁用bootJar任务，因为这是一个库模块而不是可执行应用
    if (plugins.hasPlugin("org.springframework.boot")) {
        tasks.findByName("bootJar")?.enabled = false
    }
    
    // 启用普通jar任务
    tasks.findByName("jar")?.enabled = true
}