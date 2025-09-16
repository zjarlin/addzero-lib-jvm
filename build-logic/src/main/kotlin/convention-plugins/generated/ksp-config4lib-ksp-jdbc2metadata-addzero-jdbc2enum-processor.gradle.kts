plugins {
    id("com.google.devtools.ksp")
}

ksp {
    arg("enumOutputPackage", "site.addzero.generated.enums")
    arg("dictTableName", "sys_dict")
    arg("dictIdColumn", "id")
    arg("dictCodeColumn", "dict_code")
    arg("dictNameColumn", "dict_name")
    arg("dictItemTableName", "sys_dict_item")
    arg("dictItemForeignKeyColumn", "dict_id")
    arg("dictItemCodeColumn", "item_value")
    arg("dictItemNameColumn", "item_text")
}
