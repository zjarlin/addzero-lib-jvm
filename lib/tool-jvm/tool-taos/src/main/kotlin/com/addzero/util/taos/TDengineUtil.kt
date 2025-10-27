package com.addzero.util.taos

import cn.hutool.core.bean.BeanUtil
import com.taosdata.jdbc.TSDBDriver
import java.lang.reflect.Method
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author guolinyuan
 */
class TDengineUtil {
    private val connection: Connection
    private val databaseColumnHumpToLine: Boolean

    /**
     * @param url                      url 例如 ： "jdbc:TAOS://127.0.0.1:6020/netuo_iot"
     * @param username                 例如： "root"
     * @param password                 例如： "taosdata"
     * @param databaseColumnHumpToLine 是否需要数据库列名下划线转驼峰
     */
    constructor(url: String, username: String?, password: String?, databaseColumnHumpToLine: Boolean) {
        // 解析URL获取数据库名称
        val dbPattern = Regex("/([a-zA-Z0-9_]+)$")
        val matchResult = dbPattern.find(url)
        val databaseName = matchResult?.groupValues?.get(1)

        // 先连接到服务器（不指定具体数据库）
        val baseUrl = url.substringBeforeLast("/")
        val connProps = Properties()
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_USER, username)
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_PASSWORD, password)
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_CONFIG_DIR, "/etc/taos")
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_CHARSET, "UTF-8")
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_LOCALE, "en_US.UTF-8")
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_TIME_ZONE, "UTC-8")

        // 建立初始连接
        val tempConnection = DriverManager.getConnection(baseUrl, connProps)

        // 检查并创建数据库（如果需要）
        if (databaseName != null) {
            ensureDatabaseExists(tempConnection, databaseName)
        }

        // 关闭临时连接
        tempConnection.close()

        // 重新连接到指定数据库
        this.connection = DriverManager.getConnection(url, connProps)
        this.databaseColumnHumpToLine = databaseColumnHumpToLine
    }

    /**
     * 确保数据库存在，如果不存在则创建
     */
    private fun ensureDatabaseExists(connection: Connection, databaseName: String) {
        try {
            // 尝试使用数据库
            val statement = connection.createStatement()
            statement.execute("use $databaseName")
            statement.close()
        } catch (e: SQLException) {
            // 检查是否是数据库不存在的错误
            if (e.message?.contains("not exist", ignoreCase = true) == true) {
                // 数据库不存在，创建它
                createDatabaseIfNotExists(connection, databaseName)
            } else {
                // 其他SQL异常，重新抛出
                throw e
            }
        }
    }

    /**
     * 如果数据库不存在则创建数据库
     */
    private fun createDatabaseIfNotExists(connection: Connection, databaseName: String) {
        val statement = connection.createStatement()
        statement.execute("create database if not exists $databaseName")
        statement.execute("use $databaseName")
        statement.close()
    }

    /**
     * @param connection
     * @param databaseColumnHumpToLine
     */
    constructor(connection: Connection, databaseColumnHumpToLine: Boolean) {
        this.connection = connection
        this.databaseColumnHumpToLine = databaseColumnHumpToLine
    }


    /**
     * 执行sql（无论是否返回结果），将结果注入到指定的类型实例中，且返回
     * 当查询到的数据大于一个时，取第一个
     *
     *
     * 对象遵从以下说明<br></br>
     * 1.对象字段为String类型，数据库类型(通过jdbc读取到的)无论什么类型，都将调用Object.toString方法注入值<br></br>
     * 2.对象字段为数据库类型(通过jdbc读取到的)一致的情况下，将会直接注入<br></br>
     * 3.对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试使用[Class.cast]方法转型，失败此值会是类型默认值(故实体推荐使用封装类型)<br></br>
     * 4.对象字段为[Date]时，数据库类型为Date才可以注入，如果为long(例如TDengine)将会被当作毫秒的时间戳注入<br></br>
     *
     * @param sql   要执行的sql
     * @param clazz 要注入的实体类型
     * @param <T>   要注入的实体类型
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
    </T> */
    @Throws(IllegalAccessException::class, InstantiationException::class, SQLException::class)
    fun <T> getOne(sql: String?, clazz: Class<T>): T? {
        val setterMethods: Array<Method> = getSetterMethods(clazz)
        val resultSet = connection.createStatement().executeQuery(sql)

        //只有一个结果直接下一个就行
        resultSet.next()

        return resultSetToObject(resultSet, setterMethods, clazz)
    }


    /**
     * 执行sql（无论是否返回结果），将结果注入到指定的类型实例中，且返回
     * 当查询到的结果没有时，返回一个大小为0的list;
     *
     *
     * 对象遵从以下说明<br></br>
     * 1.对象字段为String类型，数据库类型(通过jdbc读取到的)无论什么类型，都将调用Object.toString方法注入值<br></br>
     * 2.对象字段为数据库类型(通过jdbc读取到的)一致的情况下，将会直接注入<br></br>
     * 3.对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试使用[Class.cast]方法转型，失败此值会是类型默认值(故实体推荐使用封装类型)<br></br>
     * 4.对象字段为[Date]时，数据库类型为Date才可以注入，如果为long(例如TDengine)将会被当作毫秒的时间戳注入<br></br>
     *
     * @param sql   要执行的sql
     * @param clazz 要注入的实体类型
     * @param <T>   要注入的实体类型
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
    </T> */
    @Throws(IllegalAccessException::class, InstantiationException::class, SQLException::class)
    fun <T> getList(sql: String?, clazz: Class<T>): MutableList<T?> {
        val list = ArrayList<T?>()

        val setterMethods: Array<Method> = getSetterMethods(clazz)
        val resultSet = connection.createStatement().executeQuery(sql)

        while (resultSet.next()) {
            list.add(resultSetToObject(resultSet, setterMethods, clazz))
        }
        return list
    }

    /**
     * 插入对象到指定的表里面
     *
     * @param tableName
     * @param o
     * @return
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun insert(tableName: String?, o: Any?): Boolean {
        val map = BeanUtil.beanToMap(o)

        val sql: String = createInsertSql(tableName, map)
        return connection.createStatement().execute(sql)
    }


    @Throws(SQLException::class)
    fun insertWithStable(tableName: String?, sTableName: String?, o: Any?): Boolean {
        // 确保超级表存在
        ensureStableExists(sTableName, o)

        // 从对象中提取标签值
        val tagValues = extractTagValues(o)

        // 从对象中提取普通字段（排除标签字段）
        val map = BeanUtil.beanToMap(o).toMutableMap()
        removeTagFieldsFromMap(o, map)

        // 打印调试信息
//        println("Map keys: ${map.keys}")
//        println("Tag values: $tagValues")

        val sql: String = createInsertStableSql(tableName, sTableName, map, *tagValues.toTypedArray())
//        println("SQL: $sql")
        return connection.createStatement().execute(sql)
    }

    /**
     * 从映射中移除标签字段，因为标签字段不应该作为普通字段插入
     */
    private fun removeTagFieldsFromMap(o: Any?, map: MutableMap<String, Any?>) {
        if (o == null) return

        val clazz = o.javaClass
        val fields = clazz.declaredFields

        // 移除标签字段
        for (field in fields) {
            field.isAccessible = true
            if (field.isAnnotationPresent(TdTag::class.java)) {
                val tagAnnotation = field.getAnnotation(TdTag::class.java)
                // 使用注解中指定的名称，如果没有指定则使用字段名的下划线形式
                // 但要注意，在BeanUtil.beanToMap转换后的Map中，键是驼峰命名格式
                val fieldName = if (tagAnnotation.name.isNotEmpty()) {
                    // 如果注解中指定了name，则查找Map时需要找到对应的驼峰命名
                    // 因为BeanUtil.beanToMap使用的是字段原始名称(驼峰格式)
                    field.name
                } else {
                    // 如果没有指定name，则使用字段名转换成下划线的形式
                    humpToLine(field.name)
                }
                // 从BeanUtil.beanToMap得到的Map中，键是字段的原始名称（驼峰格式）
                // 所以我们应该使用字段的原始名称来移除
                map.remove(field.name)
            }
        }
    }

    /**
     * 从对象中提取标签值
     */
    private fun extractTagValues(o: Any?): List<String> {
        if (o == null) return emptyList()

        val tagValues = mutableListOf<String>()
        val clazz = o.javaClass
        val fields = clazz.declaredFields

        // 按顺序提取标签字段的值
        for (field in fields) {
            field.isAccessible = true
            if (field.isAnnotationPresent(TdTag::class.java)) {
                val value = field.get(o)?.toString() ?: ""
                tagValues.add(value)
            }
        }

        return tagValues
    }

    /**
     * 确保超级表存在，如果不存在则创建
     */
    private fun ensureStableExists(sTableName: String?, o: Any?) {
        if (sTableName == null || o == null) return

        try {
            // 尝试查询超级表
            val statement = connection.createStatement()
            statement.execute("describe $sTableName")
            statement.close()
        } catch (e: SQLException) {
            // 检查是否是表不存在的错误
            if (e.message?.contains("not exist", ignoreCase = true) == true ||
                e.message?.contains("Table does not exist", ignoreCase = true) == true) {
                // 超级表不存在，创建它
                createStableIfNotExists(sTableName, o)
            } else {
                // 其他SQL异常，重新抛出
                throw e
            }
        }
    }

    /**
     * 如果超级表不存在则创建超级表
     */
    private fun createStableIfNotExists(sTableName: String, o: Any) {
        // 获取对象的类
        val clazz = o.javaClass
        val fields = clazz.declaredFields

        // 分离普通字段和标签字段
        val regularFields = mutableListOf<java.lang.reflect.Field>()
        val tagFields = mutableListOf<java.lang.reflect.Field>()

        for (field in fields) {
            field.isAccessible = true
            if (field.isAnnotationPresent(TdTag::class.java)) {
                tagFields.add(field)
            } else {
                regularFields.add(field)
            }
        }

        // 构建字段定义部分
        val fieldDefinitions = regularFields.joinToString(", ") { field ->
            val fieldName = humpToLine(field.name)
            val fieldType = field.type

            // 根据字段类型确定数据库类型
            val dbType = when (fieldType) {
                java.lang.String::class.java -> "BINARY(255)"
                java.lang.Integer::class.java, Int::class.java -> "INT"
                java.lang.Long::class.java, Long::class.java -> "BIGINT"
                java.lang.Double::class.java, Double::class.java -> "DOUBLE"
                java.lang.Float::class.java, Float::class.java -> "FLOAT"
                java.lang.Boolean::class.java, Boolean::class.java -> "BOOL"
                java.util.Date::class.java -> "TIMESTAMP"
                else -> "BINARY(255)" // 默认使用字符串类型
            }

            if (fieldName == "ts") {
                "$fieldName TIMESTAMP"
            } else {
                "$fieldName $dbType"
            }
        }

        // 构建标签定义部分
        val tagDefinitions = tagFields.joinToString(", ") { field ->
            val tagAnnotation = field.getAnnotation(TdTag::class.java)
            val tagName = if (tagAnnotation.name.isNotEmpty()) tagAnnotation.name else humpToLine(field.name)

            // 如果注解中指定了类型，则使用指定的类型，否则根据字段类型映射
            val tagType = if (tagAnnotation.type.isNotEmpty() && tagAnnotation.type != "BINARY(255)") {
                tagAnnotation.type
            } else {
                // 根据字段类型确定标签的数据库类型
                val fieldType = field.type
                when (fieldType) {
                    java.lang.String::class.java -> "BINARY(255)"
                    java.lang.Integer::class.java, Int::class.java -> "INT"
                    java.lang.Long::class.java, Long::class.java -> "BIGINT"
                    java.lang.Double::class.java, Double::class.java -> "DOUBLE"
                    java.lang.Float::class.java, Float::class.java -> "FLOAT"
                    java.lang.Boolean::class.java, Boolean::class.java -> "BOOL"
                    java.util.Date::class.java -> "TIMESTAMP"
                    else -> "BINARY(255)" // 默认使用字符串类型
                }
            }
            "$tagName $tagType"
        }

        // 如果没有标签字段，则使用默认标签
        val finalTagDefinitions = if (tagFields.isEmpty()) {
            "tag1 BINARY(255), tag2 BINARY(255), tag3 BINARY(255)"
        } else {
            tagDefinitions
        }

        val sql = "CREATE STABLE IF NOT EXISTS $sTableName ($fieldDefinitions) TAGS ($finalTagDefinitions)"

        val statement = connection.createStatement()
        statement.execute(sql)
        statement.close()
    }

    /**
     * 将resultSet注入到指定的类型实例中，且返回
     * 对象遵从以下说明<br></br>
     * 1.对象字段为String类型，数据库类型(通过jdbc读取到的)无论什么类型，都将调用Object.toString方法注入值<br></br>
     * 2.对象字段为数据库类型(通过jdbc读取到的)一致的情况下，将会直接注入<br></br>
     * 3.对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试使用[Class.cast]方法转型，失败此值会是类型默认值(故实体推荐使用封装类型)<br></br>
     * 4.对象字段为[Date]时，数据库类型为Date才可以注入，如果为long(例如TDengine)将会被当作毫秒的时间戳注入<br></br>
     *
     *
     * 注意，此方法只会注入一个结果,不会循环[ResultSet.next]方法，请从外部调用。<br></br>
     * 传入setterMethods的目的是为了方便外部循环使用此方法，这样方法内部不会重复调用，提高效率<br></br>
     *
     * @param resultSet     查询结果，一定要是[ResultSet.next]操作过的，不然没有数据
     * @param setterMethods clazz对应的所有setter方法，可以使用[this.getSetterMethods]获取
     * @param clazz         注入对象类型
     * @param <T>           注入对象类型
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
    </T> */
    @Throws(IllegalAccessException::class, InstantiationException::class)
    fun <T> resultSetToObject(resultSet: ResultSet, setterMethods: Array<Method>, clazz: Class<T>): T? {
        val result: T?
        try {
            result = clazz.newInstance()
        } catch (e: InstantiationException) {
            println("请检查类" + clazz.getCanonicalName() + "是否有无参构造方法")
            throw e
        }


        for (method in setterMethods) {
            try {
                val fieldName: String = getFieldNameBySetter(method)

                //因为标准的setter方法只会有一个参数，所以取一个就行了
                val getParamClass = method.getParameterTypes()[0]


                //获得查询的结果
                val resultObject: Any

                //是否启用驼峰转下划线规则获得数据库字段名
                if (databaseColumnHumpToLine) {
                    resultObject = resultSet.getObject(humpToLine(fieldName))
                } else {
                    resultObject = resultSet.getObject(fieldName)
                }

                //如果实体类的类型是String类型，那么无论x数据库类型是什么，都调用其toString方法获取值
                if (getParamClass == String::class.java) {
                    method.invoke(result, resultObject.toString())
                } else if (getParamClass == Date::class.java && resultObject.javaClass == Long::class.java) {
                    method.invoke(result, Date(resultObject as Long))
                } else {
                    try {
                        method.invoke(result, resultObject)
                    } catch (e: IllegalArgumentException) {
                        //对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试强制转型
                        method.invoke(result, getParamClass.cast(resultObject))
                    }
                }
            } catch (ignored: Exception) {
                //所有的转型都失败了，则使用默认值
            }
        }

        return result
    }

    companion object {
        /**
         * 生成插入Stable的sql语句
         *
         * @param tableName
         * @param map
         * @return
         */
        fun createInsertStableSql(
            tableName: String?,
            sTbaleName: String?,
            map: Map<*, *>?,
            vararg tags: String?
        ): String {
            return "INSERT INTO $tableName${stableToSQL(sTbaleName, *tags)}${mapToSQL(map)}"
        }

        private fun stableToSQL(sTableName: String?, vararg tags: String?): String {
            val tagValues = tags.filterNotNull().joinToString(", ") { "'$it'" }
            return " using $sTableName TAGS ( $tagValues ) "
        }

        /**
         * 生成插入sql语句
         *
         * @param tableName
         * @param map
         * @return
         */
        fun createInsertSql(tableName: String?, map: Map<*, *>?): String {
            return "INSERT INTO $tableName${mapToSQL(map)}"
        }

        fun mapToSQL(map: Map<*, *>?): String {
            if (map == null) return ""

            val entries = map.entries
            val keys = entries.joinToString(",") {
                val entry = it as Map.Entry<*, *>
                humpToLine(entry.key.toString())
            }
            val values = entries.joinToString(",") { entry ->
                val value = (entry as Map.Entry<*, *>).value
                when {
                    value?.javaClass == Date::class.java -> {
                        val d = value as Date
                        d.time.toString()
                    }
                    else -> "'$value'"
                }
            }

            return """ ( $keys ) VALUES( $values )"""
        }


        /**
         * 通过setter method,获取到其对应的属性名
         *
         * @param method
         * @return
         */
        fun getFieldNameBySetter(method: Method): String {
            return toLowerCaseFirstOne(method.getName().substring(3))
        }


        /**
         * 获取指定类型方法的所有的setter方法
         * 方法属性名为key，对应的方法为value
         *
         * @param clazz
         * @return
         */
        fun getSetterMethodsMap(clazz: Class<*>): MutableMap<String?, Method?> {
            val methods = clazz.getMethods()
            val setterMethods: MutableMap<String?, Method?> = HashMap<String?, Method?>(methods.size / 2)

            for (m in methods) {
                if (m.getName().startsWith("set")) {
                    setterMethods.put(toLowerCaseFirstOne(m.getName().substring(3)), m)
                }
            }
            return setterMethods
        }

        /**
         * 获取指定类型方法的所有的setter方法
         *
         * @param clazz
         * @return
         */
        fun getSetterMethods(clazz: Class<*>): Array<Method> {
            val methods = clazz.getMethods()
            val setterMethods = mutableListOf<Method>()

            for (m in methods) {
                if (m.getName().startsWith("set")) {
                    setterMethods.add(m)
                }
            }
            return setterMethods.toTypedArray()
        }

        /**
         * 首字母转小写
         */
        fun toLowerCaseFirstOne(s: String): String {
            if (Character.isLowerCase(s.get(0))) {
                return s
            } else {
                return s.get(0).lowercaseChar().toString() + s.substring(1)
            }
        }


        /**
         * 首字母转大写
         */
        fun toUpperCaseFirstOne(s: String): String {
            if (Character.isUpperCase(s.get(0))) {
                return s
            } else {
                return s.get(0).uppercaseChar().toString() + s.substring(1)
            }
        }

        private val linePattern: Pattern = Pattern.compile("_(\\w)")

        /**
         * 下划线转驼峰
         */
        fun lineToHump(str: String): String {
            var str = str
            str = str.lowercase(Locale.getDefault())
            val matcher: Matcher = linePattern.matcher(str)
            val sb = StringBuffer()
            while (matcher.find()) {
                matcher.appendReplacement(sb, matcher.group(1).uppercase(Locale.getDefault()))
            }
            matcher.appendTail(sb)
            return sb.toString()
        }

        private val humpPattern: Pattern = Pattern.compile("[A-Z]")

        /**
         * 驼峰转下划线,效率比上面高
         */
        fun humpToLine(str: String): String {
            val matcher: Matcher = humpPattern.matcher(str)
            val sb = StringBuffer()
            while (matcher.find()) {
                matcher.appendReplacement(sb, "_" + matcher.group(0).lowercase(Locale.getDefault()))
            }
            matcher.appendTail(sb)
            return sb.toString()
        }
    }
}
