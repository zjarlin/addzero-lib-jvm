package site.addzero.mybatis.auto_wrapper

import java.lang.reflect.Field
import java.lang.reflect.Modifier

internal class GroupInfoBuilder<T, R>(
    var clazz: Class<T>,
    var packFieldPostProcess: (Class<*>, Class<*>) -> MutableSet<Field>,
    var columnProcess: (Class<T>, String) -> R
) {
    fun buildGroupInfo(obj: Any): MutableList<WheresGroupInfo<T, R>> {
        val dtoClass = obj.javaClass
        val eqFields = packFieldPostProcess(clazz, dtoClass)

        val packFieldList = findFieldsToPackField(eqFields, dtoClass)

        val map = mutableMapOf<String, WheresGroupInfo<T, R>>()
        val list = mutableListOf<WheresGroupInfo<T, R>>()
        packFieldList.forEach { packField ->
            val groupName = packField.getGroupName() ?: ""
            val outerJoin = packField.getOuterJoin()
            val innerJoin = packField.getInnerJoin()

            val wheresGroupInfo = map.getOrPut(groupName) {
                val groupInfo = WheresGroupInfo<T, R>(groupName, outerJoin, innerJoin)
                list.add(groupInfo)
                groupInfo
            }

            wheresGroupInfo.outerJoin = outerJoin || wheresGroupInfo.outerJoin
            wheresGroupInfo.innerJoin = innerJoin || wheresGroupInfo.innerJoin

            val columnInfoList = packField.getColumnInfoList(obj, columnProcess)
            for (columnInfo in columnInfoList) {
                val column = columnInfo.column
                val columnGroupInfo = wheresGroupInfo.columnGroupInfoMap.getOrPut(column) {
                    val info = ColumnGroupInfo<T, R>()
                    wheresGroupInfo.columnGroupInfoMap[column] = info
                    info
                }
                columnGroupInfo.columnInfos.add(columnInfo)
            }
        }
        return list
    }

    private fun findFieldsToPackField(
        eqFields: Set<Field>,
        dtoClazz: Class<*>
    ): List<PackField> {
        val packFields = mutableListOf<PackField>()
        val hasCustomWhere = eqFields.any {
            it.isAnnotationPresent(Wheres::class.java) || it.isAnnotationPresent(Where::class.java)
        }
        eqFields.forEach { eqField ->
            try {
                if (Modifier.isStatic(eqField.modifiers) || eqField.isSynthetic) {
                    return@forEach
                }
                if (eqField.isAnnotationPresent(Wheres::class.java)) {
                    packFields.add(WheresPackField(eqField))
                } else if (eqField.isAnnotationPresent(Where::class.java)) {
                    packFields.add(WherePackField(eqField))
                } else if (!hasCustomWhere) {
                    packFields.add(DefaultPackField(eqField))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Skip fields with malformed annotations
                // This can happen if annotation has invalid default values or classloader issues
            }
        }
        return packFields
    }
}
