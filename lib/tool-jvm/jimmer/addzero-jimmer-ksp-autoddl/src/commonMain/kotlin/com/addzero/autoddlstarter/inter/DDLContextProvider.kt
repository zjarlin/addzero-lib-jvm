package com.addzero.autoddlstarter.inter

import com.addzero.autoddlstarter.context.AutoDDLSettings
import com.addzero.autoddlstarter.context.DDLContext

interface DDLContextProvider<T> {
    fun createDDLContext4KtClass(
        t: T,
        databaseType: String = AutoDDLSettings.settings.dbType
    ) : DDLContext

}
