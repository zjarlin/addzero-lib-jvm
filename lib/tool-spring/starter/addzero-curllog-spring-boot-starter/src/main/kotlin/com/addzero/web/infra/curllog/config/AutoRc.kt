package com.addzero.web.infra.curllog.config

import com.alibaba.fastjson2.JSONObject
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = ["com.addzero.web.infra.curllog"])
class AutoRc
