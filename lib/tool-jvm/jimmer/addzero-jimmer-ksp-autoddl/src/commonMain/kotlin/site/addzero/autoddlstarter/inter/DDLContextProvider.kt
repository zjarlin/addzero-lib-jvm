package site.addzero.autoddlstarter.inter

import site.addzero.autoddlstarter.context.AutoDDLSettings
import site.addzero.autoddlstarter.context.DDLContext

interface DDLContextProvider<T> {
    fun createDDLContext4KtClass(
        t: T,
        databaseType: String = AutoDDLSettings.settings.dbType
    ) : DDLContext

}
