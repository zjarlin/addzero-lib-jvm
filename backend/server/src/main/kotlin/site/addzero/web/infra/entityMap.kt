package site.addzero.web.infra

import site.addzero.model.entity.SysArea
import site.addzero.model.entity.SysUser

val entityMap = mapOf(
    "sys_user" to SysUser::class,
    "sys_area" to SysArea::class
)
