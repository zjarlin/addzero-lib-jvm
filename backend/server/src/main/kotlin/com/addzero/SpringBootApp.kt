package com.addzero

import cn.dev33.satoken.SaManager
import cn.hutool.core.net.NetUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.extra.spring.SpringUtil
import com.addzero.SpringBootApp.Companion.runSpringBootApp
import org.babyfish.jimmer.client.EnableImplicitApi
import org.babyfish.jimmer.spring.cfg.JimmerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import java.util.*

@SpringBootApplication
@EnableImplicitApi
open class SpringBootApp() {
    companion object {
        fun runSpringBootApp(args: Array<String>) {
            val run = runApplication<SpringBootApp>(*args)

//            val apply = SpringUtil.getBean(DeepSeekParentProperties::class.java)
//            println(apply.apiKey)
            println("启动成功，Sa-Token 配置如下：" + SaManager.getConfig());

            val env: Environment = run.environment
            // 获取本机IP地址
            val ip = NetUtil.getLocalhostStr()
            val port = env.getProperty("server.port")
            var property = env.getProperty("server.servlet.context-path")


            property = StrUtil.removeSuffix(property, "/")
            val path = Optional.ofNullable(property).orElse("")


            val bean = SpringUtil.getBean(JimmerProperties::class.java)
            val uiPath = bean.client.openapi.uiPath

            println(
                """
    ----------------------------------------------------------
        Application is running! Access URLs:
        Jimmer接口文档:  http://$ip:$port$path$uiPath
        Jimmer接口代理文档:  http://localhost:$port$path/ui
        Jimmer对接ts.zip:  http://localhost:$port$path/ts.zip
    ----------------------------------------------------------
    """
            )
        }
    }
}


fun main(args: Array<String>) {
    val runApp = runSpringBootApp(args)




}
