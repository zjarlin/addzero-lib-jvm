plugins {
    id("kmp-ksp")
//    id("ksp-buddy")
}

//kspBuddy {
//    mustMap = mapOf(
//        "enumOutputPackage" to "site.addzero.generated.enums",
//        "dictTableName" to "sys_dict",
//        "dictIdColumn" to "id",
//        "dictCodeColumn" to "dict_code",
//        "dictNameColumn" to "dict_name",
//        "dictItemTableName" to "sys_dict_item",
//        "dictItemForeignKeyColumn" to "dict_id",
//        "dictItemCodeColumn" to "item_value",
//        "dictItemNameColumn" to "item_text"
//    )
//}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.common.addzeroKspSupportJdbc)
            implementation(projects.lib.ksp.common.addzeroKspSupport)

        }
        jvmMain.dependencies {
        }

    }
}
