tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "-AjdbcDriver=com.mysql.cj.jdbc.Driver",
        "-AjdbcUrl=jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
        "-AjdbcUsername=root",
        "-AjdbcPassword=zljkj~123",
        "-AdictTableName=sys_dict_type",
        "-AdictIdColumn=dict_type",
        "-AdictCodeColumn=dict_type",
        "-AdictNameColumn=dict_name",
        "-AdictItemTableName=sys_dict_data",
        "-AdictItemForeignKeyColumn=dict_type",
        "-AdictItemCodeColumn=dict_value",
        "-AdictItemNameColumn=dict_label",
        "-AenumOutputPackage=site.addzero.apt.test.enums",
        "-AenumOutputDirectory=/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/apt-dict-processor/src/test/java",
    ))
}
