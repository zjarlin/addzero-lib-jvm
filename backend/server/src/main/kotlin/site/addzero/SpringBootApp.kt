package site.addzero

import cn.hutool.core.net.NetUtil.getLocalhostStr
import cn.hutool.core.text.CharSequenceUtil.removeSuffix
import cn.hutool.extra.spring.SpringUtil
import org.babyfish.jimmer.client.EnableImplicitApi
import org.babyfish.jimmer.spring.cfg.JimmerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import java.util.Optional.ofNullable

@SpringBootApplication
@EnableImplicitApi
open class SpringBootApp()


fun main(args: Array<String>) {
    val run = runApplication<SpringBootApp>(*args)
    val env: Environment = run.environment
    // 获取本机IP地址
    val ip = getLocalhostStr()
    val port = env.getProperty("server.port")
    var property = env.getProperty("server.servlet.context-path")
    property = removeSuffix(property, "/")
    val path = ofNullable(property).orElse("")
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
