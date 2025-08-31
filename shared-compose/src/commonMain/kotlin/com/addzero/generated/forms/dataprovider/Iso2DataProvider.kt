package com.addzero.generated.forms.dataprovider

import com.addzero.generated.api.ApiProvider
import com.addzero.generated.isomorphic.SysDeptIso
import com.addzero.generated.isomorphic.SysUserIso
import com.addzero.generated.isomorphic.SysRoleIso
import com.addzero.generated.isomorphic.SysDictIso
import com.addzero.generated.isomorphic.BizDotfilesIso
import com.addzero.generated.isomorphic.SysDictItemIso


object Iso2DataProvider {
    val isoToDataProvider = mapOf(
        SysDeptIso::class to ApiProvider.sysDeptApi::tree,
        SysUserIso::class to ApiProvider.sysUserApi::tree,
        SysRoleIso::class to ApiProvider.sysRoleApi::tree,
        SysDictIso::class to ApiProvider.sysDictApi::tree,
        BizDotfilesIso::class to ApiProvider.bizDotfilesApi::tree,
        SysDictItemIso::class to ApiProvider.sysDictItemApi::tree
    )
}