//package com.addzero.form_mapping
//
//import com.addzero.generated.api.ApiProvider
//import com.addzero.SysDeptIso
//import com.addzero.SysDictIso
//import com.addzero.SysDictItemIso
//import com.addzero.SysRoleIso
//import com.addzero.SysUserIso
//
//
//object Iso2DataProvider {
//    val isoToDataProvider = mapOf(
//        SysDeptIso::class to { ApiProvider.sysDeptApi::tree },
//        SysDictIso::class to { ApiProvider.sysDictApi::tree },
//        SysDictItemIso::class to { ApiProvider.sysDictItemApi::tree },
//        SysRoleIso::class to { ApiProvider.sysRoleApi::tree },
//        SysUserIso::class to { ApiProvider.sysUserApi::tree }
//    )
//}
