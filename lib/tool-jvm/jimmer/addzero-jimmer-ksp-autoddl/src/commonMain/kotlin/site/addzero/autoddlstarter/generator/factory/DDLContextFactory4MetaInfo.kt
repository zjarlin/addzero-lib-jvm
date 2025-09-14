package site.addzero.autoddlstarter.generator.factory

import site.addzero.autoddlstarter.generator.IDatabaseGenerator.Companion.getDatabaseDDLGenerator
import site.addzero.autoddlstarter.context.DDLContext
import site.addzero.autoddlstarter.context.DDlRangeContext
import site.addzero.autoddlstarter.context.AutoDDLSettings
import site.addzero.autoddlstarter.inter.DDLContextProvider
import site.addzero.util.filterBaseEntity
import site.addzero.util.getAnno
import site.addzero.util.getArg
import site.addzero.util.getArgFirstValue
import site.addzero.util.guessTableName
import site.addzero.util.hasAnno
import site.addzero.util.isCollectionType
import site.addzero.util.isCustomClassType
import site.addzero.util.isNullableFlag
import site.addzero.util.ktName
import site.addzero.util.ktType
import site.addzero.util.str.removeAnyQuote
import site.addzero.util.str.toUnderLineCase
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

private const val UNKNOWN_TABLE_NAME = "unknown_table_name"

object DDLContextFactory4JavaMetaInfo: DDLContextProvider<KSClassDeclaration> {

    override fun createDDLContext4KtClass(
        t: KSClassDeclaration,
        databaseType: String
    ): DDLContext {

        val classComment = t.docString ?: ""
        var (tableChineseName, tableEnglishName) = Pair(classComment, guessTableName(t))
        tableEnglishName = tableEnglishName!!.ifBlank { UNKNOWN_TABLE_NAME }
        tableChineseName = tableChineseName.ifBlank { tableEnglishName!! }

        val rangeContexts = extractInterfaceMetaInfo(t)

        return DDLContext(
            tableChineseName = tableChineseName.removeAnyQuote(),
            tableEnglishName = tableEnglishName.removeAnyQuote(),
            databaseType = databaseType.removeAnyQuote(),
            dto = rangeContexts,
        )

    }

    data class DoubleMapping(
        //有mappedBy 从动方
        val sourceProperty: KSPropertyDeclaration,
        val sourceClass: KSClassDeclaration = sourceProperty.parentDeclaration as KSClassDeclaration,
        val targetProperty: KSPropertyDeclaration?,
        val targetClass: KSClassDeclaration? = targetProperty?.parentDeclaration as KSClassDeclaration?,
        )


    /**
     * 提取接口字段元数据
     */
    private fun extractInterfaceMetaInfo(ktClass: KSClassDeclaration): List<DDlRangeContext> {
        val toList = ktClass.getAllProperties().map { property ->


            val ktName = property.simpleName.asString()

            val actColName = property.getAnno("Column").getArg("name") ?: ktName.toUnderLineCase()

            // 字段名
//            val colName = property.simpleName.asString()
            // 字段类型
            val ktType = property.ktType()
            // 字段注释
            val colComment = property.docString ?: ""
            // 字段长度（假设有@Length注解）
            val colLength = property.getAnno("Length").getArgFirstValue() ?: ""


            // 是否主键（假设有@Id注解）
            val anno1 = property.getAnno("Id")
            val isPrimaryKey = anno1.toString()
            // 是否自增（假设有@AutoIncrement注解或@PrimaryKey(autoIncrement = true)）
            val isSelfIncreasing = property.getAnno("AutoIncrement").toString()

            val defaultDDlContext = DDlRangeContext(
                colName = actColName.toString().removeAnyQuote(),
                colType = ktType,
                colComment = colComment.removeAnyQuote(),
                colLength = colLength,
                primaryKeyFlag = isPrimaryKey,
                selfIncreasingFlag = isSelfIncreasing,
                nullableFlag = property.isNullableFlag(),
                ktType = property.ktType(),
                ktName = property.ktName()
            )


            //特殊类型

            if (property.isCustomClassType()) {


                val anno = property.getAnno("JoinColumn")

                if (anno != null) {
                    //如果有
                    val arg = anno.getArg("name")
                    if (arg != null) {
                        return@map defaultDDlContext.copy(colName = arg.toString())
                    } else {

                        return@map defaultDDlContext.copy(
                            colName = property.ktName().toUnderLineCase() + "_id"
                        )
                    }

                }





            }
            if (property.isCollectionType()) {

                //不在这里处理集合
                return@map null

            }


            if (property.hasAnno("Formula")|| property.hasAnno("Transient")) {
                //不在这里处理计算属性
                return@map null
            }



            DDlRangeContext(
                colName = actColName.toString().removeAnyQuote(),
                colType = ktType,
                colComment = colComment.removeAnyQuote(),
                colLength = colLength,
                primaryKeyFlag = isPrimaryKey,
                selfIncreasingFlag = isSelfIncreasing,
                nullableFlag = property.isNullableFlag(),
                ktType = ktType,
                ktName = ktName

            )
        }
            .filterNotNull()
            .filter {

                filterBaseEntity(it.colName)

            }
            .map { handlerTypeMapping(it) }
            .toList()
        return toList
    }

    private fun handlerTypeMapping(it: DDlRangeContext): DDlRangeContext {
        val databaseDDLGenerator = getDatabaseDDLGenerator(AutoDDLSettings.settings.dbType)
        val defaultStringType = databaseDDLGenerator.defaultStringType
        val selfMappingTypeTable = databaseDDLGenerator.selfMappingTypeTable
        val oldType = it.ktType
        val copy = it.copy(colType = selfMappingTypeTable[oldType] ?: defaultStringType)
        return copy
    }

//    fun filterBaseEntity(colName: String): Boolean {
//        val settings = AutoDDLSettings.settings
//        val id = settings.id
//        val createBy = settings.createBy
//        val updateBy = settings.updateBy
//        val createTime = settings.createTime
//        val updateTime = settings.updateTime
//
//        if (colName.isNullOrEmpty()) {
//            return false
//        }
//        val arrayOf = arrayOf(id, createBy, updateBy, createTime, updateTime)
//        val arrayOf1 = arrayOf.map { it.toLowCamelCase() }.toTypedArray()
//
//        val containsAny =colName.containsAny(*arrayOf)
//        val containsAny1 = colName.containsAny(*arrayOf1)
//        val b = !(containsAny || containsAny1)
//        return b
//    }


}


