package com.addzero.web.modules.controller

import com.addzero.web.infra.jackson.toJson
import com.alibaba.fastjson2.JSONObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestCurloggController {

    @GetMapping("/test")
    fun djoiasdjoijO(): Int {
        return 1 / 0
    }


}
