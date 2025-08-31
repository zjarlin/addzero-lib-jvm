package com.addzero.web.modules.controller

import cn.hutool.extra.spring.SpringUtil
import com.addzero.model.entity.BizDotfiles
import com.addzero.web.infra.jimmer.base.BaseTreeApi
import org.springframework.core.ResolvableType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/bizDotfiles")
class BizDotfilesController : BaseTreeApi<BizDotfiles> {

    @GetMapping("/page")
    fun page(): Unit {
    }

    @PostMapping("/save")
    fun save(): Unit {
        // TODO:
    }

}
