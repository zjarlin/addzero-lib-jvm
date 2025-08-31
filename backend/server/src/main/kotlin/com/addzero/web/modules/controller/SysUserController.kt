package com.addzero.web.modules.controller

import com.addzero.common.consts.sql
import com.addzero.entity.PageResult
import com.addzero.model.entity.SysUser
import com.addzero.web.infra.jackson.convertTo
import com.addzero.web.infra.jimmer.base.BaseTreeApi
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sysUser")
class SysUserController : BaseTreeApi<SysUser> {

    @GetMapping("/page")
    fun page(): PageResult<SysUser> {
        val fetchPage = sql.createQuery(SysUser::class) {
            select(table)
        }.fetchPage(0, 10)
        return fetchPage.convertTo<PageResult<SysUser>>()
    }

    @PostMapping("/save")
    fun save(): Unit {
        // TODO:
    }

}
