package site.addzero.generated.forms.dataprovider

import site.addzero.generated.api.ApiProvider
import site.addzero.generated.isomorphic.SysDeptIso
import site.addzero.generated.isomorphic.SysUserIso
import site.addzero.generated.isomorphic.SysRoleIso
import site.addzero.generated.isomorphic.SysDictIso
import site.addzero.generated.isomorphic.BizDotfilesIso
import site.addzero.generated.isomorphic.SysDictItemIso


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