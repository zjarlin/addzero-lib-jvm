package com.addzero.processor

import cn.hutool.json.JSONUtil
import com.addzero.autoddlstarter.context.DDLContext
import com.addzero.autoddlstarter.context.DDlRangeContext
import com.addzero.autoddlstarter.context.SettingContext
import com.addzero.autoddlstarter.generator.IDatabaseGenerator.Companion.getDatabaseDDLGenerator
import com.addzero.autoddlstarter.generator.factory.DDLContextFactory4JavaMetaInfo
import com.addzero.autoddlstarter.generator.factory.DDLContextFactory4JavaMetaInfo.createDDLContext4KtClass
import com.addzero.autoddlstarter.util.*
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration


class JimmerEntity2DDLProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    //多轮编译要在成员用ret接收,如果在process直接收集map DDLContext则会覆盖元数据
    val ret = mutableSetOf<DDLContext>()


    private fun handleManyToMany(entities: Sequence<KSClassDeclaration>) {

        val dbType = SettingContext.settings.dbType
        val idType = SettingContext.settings.idType

        val dict = mutableMapOf<String, KSPropertyDeclaration>()

        entities.forEach {
            it.getAllProperties().forEach { it ->

                val asString = it.simpleName.asString()
                dict.put(asString, it)
            }
        }


        val youMapperdBy的Kclass = entities.filter { en ->

            en.getAllProperties().any { property ->
                val anno = property.getAnno("ManyToMany")
                val annoJoin = property.getAnno("JoinTable")!=null
                val bool = anno != null
                val arg = anno.getArg("mappedBy") != null
                bool && arg

//                ||bool&&annoJoin
            }


        }


        val allProperties = youMapperdBy的Kclass.flatMap { it.getAllProperties() }.toList()
//        allProperties.filter { it.hasAnno("") }


        val filter = handleRet(allProperties, dict, dbType, idType, ret)

        //情况2

//           @ManyToMany
//    @JoinTable(
//        name = "biz_mapping",
//        joinColumnName = "from_id",
//        inverseJoinColumnName = "to_id",
//        filter = JoinTable.JoinTableFilter(
//            columnName = "mapping_type",
//            values = ["note_tag_mapping"]
//        )
//    )
//    val tags: List<BizTag>

        val youJoinTableDeClass = entities.filter { en ->

            en.getAllProperties().any { property ->
                val anno = property.getAnno("ManyToMany")
                val bool = anno != null

                val anno1 = property.getAnno("JoinTable")

                val bool1 = anno1 != null
                bool && bool1
            }
        }.flatMap { it.getAllProperties() }
        val size = youJoinTableDeClass.toList().size
        println("既有mm又有jointable$size")


        youJoinTableDeClass.forEach {
            val anno1 = it.getAnno("JoinTable")

            val mmtableName = anno1.getArg("name").toString()
            val fromId = anno1.getArg("joinColumnName").toString()
            val toId = anno1.getArg("inverseJoinColumnName").toString()
            if (fromId.isNotBlank() && toId.isNotBlank()) {
                val ddlContext = contextRes(mmtableName, dbType, fromId, idType, toId)

                if (ddlContext.dto.none { it.colName == "_id" }) {
                    ret.add(ddlContext)
                }
            }


        }


//    entities.map { en ->
//
//        val filter1 = en.getAllProperties().filter { property ->
//            val anno = property.getAnno("ManyToMany")
//            val bool = anno != null
//            val arg = anno.getArg("mappedBy") != null
//            bool && arg
//        }.toList()
//        println("有mappedBy的${filter1.size}")
//        val filter = handleRet(filter1, dict, dbType, idType, ret)
//
//
//
//        filter
//    }
//        genCode("/Users/zjarlin/Downloads/AddzeroKmp/aaa.json", ret.toJson())

    }


    override fun process(resolver: Resolver): List<KSAnnotated> {
        SettingContext.initialize(environment.options)

        val entities =
            resolver.getSymbolsWithAnnotation("org.babyfish.jimmer.sql.Entity").filterIsInstance<KSClassDeclaration>()

        entities.forEach {
            val extractTableMeta = extractTableMeta(it)
            ret.add(extractTableMeta)
        }

        handleManyToMany(entities)

        return emptyList()
    }

    override fun finish() {

        val resourceDir = environment.options["serverResourceDir"] ?: throw IllegalArgumentException("autoddl模块资源目录(serverResourceDir)未设置，请在build.gradle.kts中配置")

        val metaJsonSavePath = environment.options["metaJsonSavePath"] ?: "db/autoddl/meta"

        val metaJsonSaveName = environment.options["metaJsonSaveName"] ?: "jimmer_ddlcontext.json"

        val metaJsonFinalSavePath = "$resourceDir/$metaJsonSavePath/$metaJsonSaveName"
        println("最终metajson生成路径为$metaJsonFinalSavePath")

        val sqlSavePath = environment.options["sqlSavePath"] ?: "db/autoddl"


        val toJsonPrettyStr = JSONUtil.toJsonPrettyStr(ret)


        genCode(metaJsonFinalSavePath, toJsonPrettyStr)


        val databaseDDLGenerator = getDatabaseDDLGenerator(SettingContext.settings.dbType)

        ret.forEachIndexed { index, context ->
            val tableEnglishName = context.tableEnglishName
            val sqlName = "V${index}__${tableEnglishName}_init.sql"
            val sqlSaveFinalPath = "$resourceDir/$sqlSavePath/$sqlName"
            val createTableSql = databaseDDLGenerator.generateCreateTableDDL(context)
            genCode(sqlSaveFinalPath, createTableSql)
        }


    }


    fun extractTableMeta(classDecl: KSClassDeclaration): DDLContext {
        val createDDLContext4KtClass = createDDLContext4KtClass(classDecl)
        return createDDLContext4KtClass

    }

}


private fun handleRet(
    filter1: List<KSPropertyDeclaration>,
    dict: MutableMap<String, KSPropertyDeclaration>,
    dbType: String,
    idType: String,
    ret: MutableSet<DDLContext>
) {
    val filter = filter1.forEach { property ->


        val anno = property.getAnno("ManyToMany")


        val mapperdBy = anno.getArg("mappedBy").toString()


        val doubleMapping = DDLContextFactory4JavaMetaInfo.DoubleMapping(
            sourceProperty = property,
            targetProperty = dict[mapperdBy],
        )

        //如果这上面有的话
        val anno1 = doubleMapping.targetProperty?.getAnno("JoinTable")




        when {
            doubleMapping.targetProperty?.getAnno("JoinTable") != null -> {

                val mmtableName = anno1.getArg("name").toString()
                val fromId = anno1.getArg("joinColumnName").toString()
                val toId = anno1.getArg("inverseJoinColumnName").toString()
                if (fromId.isNotBlank() && toId.isNotBlank()) {
                    val ddlContext = contextRes(mmtableName, dbType, fromId, idType, toId)
                    if (ddlContext.dto.none { it.colName == "_id" }) {
                        ret.add(ddlContext)

                    }
                }
            }

            else -> {


                val zhongjianClassName = doubleMapping.sourceClass.simpleName.asString().toUnderlineCase()
                val zuobianClassName = doubleMapping.targetClass?.simpleName?.asString().toUnderlineCase()
                if (zhongjianClassName.isNotBlank() && zhongjianClassName.isNotBlank()) {
                    val defaultmmTableName = "${zuobianClassName}_${zhongjianClassName}_mapping"
                    val defaultFormId = "${zuobianClassName}_id"
                    val defaultToId = "${zhongjianClassName}_id"


                    val ddlContext = contextRes(defaultmmTableName, dbType, defaultFormId, idType, defaultToId)


                    if (ddlContext.dto.none { it.colName == "_id" }) {
                        ret.add(ddlContext)

                    }


                }


            }
        }
    }
    return filter
}

private fun contextRes(
    mmtableName: String, dbType: String, fromId: String, idType: String, toId: String
): DDLContext {
    val ddlContext = DDLContext(
        tableChineseName = "", tableEnglishName = mmtableName, databaseType = dbType, databaseName = "", dto = listOf(
            DDlRangeContext(
                ktName = "",
                colName = fromId,
                colType = idType,
                colComment = "",
                colLength = "",
                primaryKeyFlag = "",
                selfIncreasingFlag = "",
                nullableFlag = "NOT NULL",
                ktType = ""
            ), DDlRangeContext(
                ktName = "",
                colName = toId,
                colType = idType,
                colComment = "",
                colLength = "",
                primaryKeyFlag = "",
                selfIncreasingFlag = "",
                nullableFlag = "NOT NULL",
                ktType = ""
            )
        )
    )
    return ddlContext
}

/**
 * 处理器提供者
 */
class JimmerEntity2DDLProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return JimmerEntity2DDLProcessor(environment)
    }

}
