package com.addzero.util.taos

import com.taosdata.jdbc.TSDBDriver
import net.sf.cglib.beans.BeanMap
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
//        val forName = Class.forName("com.taosdata.jdbc.TSDBDriver")
        val connProps = Properties()
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_USER, username)
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_PASSWORD, password)
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_CONFIG_DIR, "/etc/taos")
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_CHARSET, "UTF-8")
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_LOCALE, "en_US.UTF-8")
        connProps.setProperty(TSDBDriver.PROPERTY_KEY_TIME_ZONE, "UTC-8")
        this.connection = DriverManager.getConnection(url, connProps)
        this.databaseColumnHumpToLine = databaseColumnHumpToLine
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
        val map= BeanMap.create(o)

        val sql: String = createInsertSql(tableName, map)
        return connection.createStatement().execute(sql)
    }


    @Throws(SQLException::class)
    fun insertWithStable(tableName: String?, sTableName: String?, o: Any?, vararg tags: String?): Boolean {
        val map = BeanMap.create(o)

        val sql: String = createInsertStableSql(tableName, sTableName, map, *tags)
        return connection.createStatement().execute(sql)
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
            map: BeanMap?,
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
        fun createInsertSql(tableName: String?, map: BeanMap?): String {
            return "INSERT INTO $tableName${mapToSQL(map)}"
        }

        fun mapToSQL(map: BeanMap?): String {
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
