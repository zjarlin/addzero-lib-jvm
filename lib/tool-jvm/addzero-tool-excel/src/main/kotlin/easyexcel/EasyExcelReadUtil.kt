//package com.gisroad.business.util.easyexcel
//
//import cn.hutool.core.bean.BeanUtil
//import com.gisroad.business.util.easyexcel.EasyExcelReadUtil.readExcelWithSpo
//import org.apache.commons.lang3.tuple.Pair
//import org.apache.commons.lang3.tuple.Triple
//import java.io.*
//import java.lang.reflect.Field
//import java.net.URLEncoder
//import java.util.*
//import java.util.function.BinaryOperator
//import java.util.function.Function
//import java.util.stream.Collectors
//
///**
// * excel读工具类
// *
// * @author zjarlin
// * @since 2023/04/30
// */
//object EasyExcelReadUtil {
//    fun readMap(reader: cn.hutool.poi.excel.ExcelReader): MutableList<MutableMap<String?, Any?>?>? {
//        val list: MutableList<MutableMap<String?, Any?>?>? = reader.readAll()
//        return list
//    }
//
//    fun readMap(path: String?): MutableList<MutableMap<String?, Any?>?>? {
//        val reader: cn.hutool.poi.excel.ExcelReader = ExcelUtil.getReader(path)
//        val list1: MutableList<MutableMap<String?, Any?>?>? = EasyExcelReadUtil.readMap(reader)
//        return list1
//    }
//
//    fun readMap(`in`: InputStream?): MutableList<MutableMap<String?, Any?>?>? {
//        val reader: cn.hutool.poi.excel.ExcelReader = ExcelUtil.getReader(`in`)
//        val list1: MutableList<MutableMap<String?, Any?>?>? = EasyExcelReadUtil.readMap(reader)
//        return list1
//    }
//
//
//    fun <T> read(filePath: String, clazz: Class<in T?>?, sheetNo: Int): MutableList<T?>? {
//        val f = File(filePath)
//        try {
//            FileInputStream(f).use { fis ->
//                return read<T?>(fis, clazz, sheetNo)
//            }
//        } catch (e: FileNotFoundException) {
//            log.error("文件{}不存在", filePath, e)
//        } catch (e: IOException) {
//            log.error("文件读取出错", e)
//        }
//
//        return null
//    }
//
//    fun <T> read(inputStream: InputStream, clazz: Class<in T?>?, sheetNo: Int): MutableList<T?> {
//        if (inputStream == null) {
//            throw RuntimeException("解析出错了，文件流是null")
//        }
//        // 有个很重要的点 DataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
//        val listener: DataListener<T?> = DataListener<Any?>()
//        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
//        EasyExcel.read(inputStream, clazz, listener) // 需要读取批注 默认不读取(批注版本不支持,找不到方法,先注释)
//            //                .extraRead(CellExtraTypeEnum.COMMENT)
//            // 需要读取超链接 默认不读取
//            .extraRead(CellExtraTypeEnum.HYPERLINK) // 需要读取合并单元格信息 默认不读取
//            .extraRead(CellExtraTypeEnum.MERGE).sheet(sheetNo).doRead()
//        return listener.getRows()
//    }
//
//    fun <T> read(filePath: String, clazz: Class<in T?>?, sheetName: String?): MutableList<T?>? {
//        val f = File(filePath)
//        try {
//            FileInputStream(f).use { fis ->
//                return read<T?>(fis, clazz, sheetName)
//            }
//        } catch (e: FileNotFoundException) {
//            log.error("文件{}不存在", filePath, e)
//        } catch (e: IOException) {
//            log.error("文件读取出错", e)
//        }
//
//        return null
//    }
//
//    fun <T> read(inputStream: InputStream, clazz: Class<in T?>?, sheetName: String?): MutableList<T?> {
//        if (inputStream == null) {
//            throw RuntimeException("解析出错了，文件流是null")
//        }
//        // 有个很重要的点 DataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
//        val listener: DataListener<T?> = DataListener<Any?>()
//        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
//        EasyExcel.read(inputStream, clazz, listener) // 需要读取批注 默认不读取(批注版本不支持,找不到方法,先注释)
//            //                .extraRead(CellExtraTypeEnum.COMMENT)
//            // 需要读取超链接 默认不读取
//            .extraRead(CellExtraTypeEnum.HYPERLINK) // 需要读取合并单元格信息 默认不读取
//            .extraRead(CellExtraTypeEnum.MERGE)
//
//            .sheet(sheetName).doRead()
//        return listener.getRows()
//    }
//
//    private fun <T> getTs(inputStream: InputStream, clazz: Class<in T?>?, sheetName: String?): MutableList<T?> {
//        if (inputStream == null) {
//            throw RuntimeException("解析出错了，文件流是null")
//        }
//
//        // 有个很重要的点 DataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
//        val listener: DataListener<T?> = DataListener<Any?>()
//
//        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
//        EasyExcel.read(inputStream, clazz, listener).sheet(sheetName).doRead()
//        return listener.getRows()
//    }
//
//    fun write(outFile: String?, list: MutableList<*>) {
//        val clazz: Class<*> = list.get(0)!!.javaClass
//        // 新版本会自动关闭流，不需要自己操作
//        EasyExcel.write(outFile, clazz).sheet().doWrite(list)
//    }
//
//    fun write(outFile: String?, list: MutableList<*>, sheetName: String?) {
//        val clazz: Class<*> = list.get(0)!!.javaClass
//        // 新版本会自动关闭流，不需要自己操作
//        EasyExcel.write(outFile, clazz).sheet(sheetName).doWrite(list)
//    }
//
//    fun write(outputStream: OutputStream?, list: MutableList<*>, sheetName: String?) {
//        val clazz: Class<*> = list.get(0)!!.javaClass
//        // 新版本会自动关闭流，不需要自己操作
//        // sheetName为sheet的名字，默认写第一个sheet
//        EasyExcel.write(outputStream, clazz).sheet(sheetName).doWrite(list)
//    }
//
//    /**
//     * 文件下载（失败了会返回一个有部分数据的Excel），用于直接把excel返回到浏览器下载
//     */
//    @Throws(IOException::class)
//    fun download(response: HttpServletResponse, list: MutableList<*>, sheetName: String) {
//        val clazz: Class<*> = list.get(0)!!.javaClass
//
//        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//        response.setCharacterEncoding("utf-8")
//        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
//        val fileName = URLEncoder.encode(sheetName, "UTF-8").replace("\\+".toRegex(), "%20")
//        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
//        EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(list)
//    }
//
//    /**
//     * 根据表索引得到表名
//     *
//     * @param filePath   文件路径
//     * @param sheetIndex 表索引 入参
//     * @return [String]
//     * @author addzero
//     * @since 2022/11/11
//     */
//    fun getSheetNameByNo(filePath: String?, sheetIndex: Int): String? {
//        val excelReaderBuilder: ExcelReaderBuilder = EasyExcel.read(filePath)
//        val excelReader: ExcelReader = excelReaderBuilder.build()
//        val sheets: MutableList<ReadSheet?> = excelReader.excelExecutor().sheetList()
//        val collect: MutableMap<Int?, String?> =
//            sheets.stream().collect(Collectors.toMap(ReadSheet::getSheetNo, ReadSheet::getSheetName))
//        return collect.get(sheetIndex)
//    }
//
//    fun <T> readExcelWithSpo(
//        fileName: String?,
//        entityClass: Class<T?>?,
//        keyFunction: Function<T?, *>?,
//        contextTableName: String?
//    ): Pair<MutableList<T?>?, MutableList<Spo?>?> {
//        return readExcelWithSpo(EasyExcel.read(fileName), entityClass, keyFunction, contextTableName)
//    }
//
//    fun <T> readExcelWithSpo(
//        inputStream: InputStream?,
//        entityClass: Class<T?>?, keyFunction: Function<T?, *>?, contextTableName: String?
//    ): Pair<MutableList<T?>?, MutableList<Spo?>?> {
//        return readExcelWithSpo(EasyExcel.read(inputStream), entityClass, keyFunction, contextTableName)
//    }
//
//
//    fun <T> readExcelWithSpoTriple(
//        excelReaderBuilder: ExcelReaderBuilder,
//        entityClass: Class<T?>?,
//        keyFunction: Function<T?, *>,
//        contextTableName: String?
//    ): MutableMap<String?, Pair<T?, MutableList<Spo?>?>?> {
//        val myHeadMap: BiMap<Int?, String?> = BiMap(HashMap<K?, V?>())
//
//        val ret: MutableMap<String?, Any?> = HashMap<String?, Any?>()
//        val ret2: MutableMap<String?, Any?> = HashMap<String?, Any?>()
//        val retList: MutableList<Triple<String?, T?, MutableList<Spo?>?>?> =
//            ArrayList<Triple<String?, T?, MutableList<Spo?>?>?>()
//
//        val readListener: AnalysisEventListener<MutableMap<Int?, Any?>?> =
//            object : AnalysisEventListener<MutableMap<Int?, Any?>?>() {
//                public override fun extra(extra: CellExtra?, context: AnalysisContext?) {
//                    super.extra(extra, context)
//                }
//
//                public override fun invokeHeadMap(headMap: MutableMap<Int?, String?>?, context: AnalysisContext?) {
//                    myHeadMap.putAll(headMap)
//                }
//
//                public override fun invoke(data: MutableMap<Int?, Any?>, context: AnalysisContext?) {
//                    val tHeaderMap: BiMap<Int?, String?> = BiMap(HashMap<K?, V?>())
//                    val declaredFields: Array<Field?> = ReflectUtil.getFields(entityClass)
//
//                    Arrays.stream<Field?>(declaredFields)
//                        .filter { field: Field? -> field!!.isAnnotationPresent(ExcelProperty::class.java) }
//                        .forEach { field: Field? ->
//                            field!!.setAccessible(true)
//                            val annotation: ExcelProperty = field.getAnnotation<T?>(ExcelProperty::class.java)
//                            val headerName: String? = annotation.value()[0]
//                            val i: Int? = myHeadMap.getInverse().get(headerName)
//                            tHeaderMap.put(i, field.getName())
//                        }
//
//                    val tIndexes: MutableSet<Int?>? = tHeaderMap.keySet()
//
//                    myHeadMap.entrySet().forEach({ h ->
//                        val index: Int? = h.getKey()
//                        val headerName: String? = h.getValue()
//                        val fieldName: String? = tHeaderMap.get(index)
//                        val value = data.getOrDefault(index, null)
//                        if (CollUtil.contains(tIndexes, index)) {
//                            ret.put(fieldName, value)
//                        } else {
//                            ret2.put(headerName, value)
//                        }
//                    })
//
//                    val entity: T? = BeanUtil.mapToBean(ret, entityClass, null)
//
//                    // 获取 subject 值，即 keyFunction 的结果
//                    val subject: String? = Convert.toStr(keyFunction.apply(entity))
//
//                    // 构造 SPO 列表
//                    val spoList: MutableList<Spo?> =
//                        ret2.entries.stream().map<Any?> { e: MutableMap.MutableEntry<String?, Any?>? ->
//                            val headerName = e!!.key
//                            val value = e.value
//                            val `val`: String? = Convert.toStr(value)
//                            Spo(subject, headerName, `val`, contextTableName)
//                        }
//                            .filter { spo: Any? -> StrUtil.isNotBlank(spo.getObject()) }
//                            .collect(Collectors.toList())
//
//                    // 将结果添加到 Triple 列表中
//                    retList.add(Triple.of<String?, T?, MutableList<Spo?>?>(subject, entity, spoList))
//                }
//
//                public override fun doAfterAllAnalysed(context: AnalysisContext?) {
//                }
//            }
//
//        excelReaderBuilder.registerReadListener(readListener)
//        excelReaderBuilder.sheet().doRead()
//
//        val collect: MutableMap<String?, Pair<T?, MutableList<Spo?>?>?> = retList.stream()
//            .filter { e: Triple<String?, T?, MutableList<Spo?>?>? -> Objects.nonNull(e) }
//            .collect(
//                Collectors.toMap(
//                    Function { e: Triple<String?, T?, MutableList<Spo?>?>? -> e!!.getLeft() },
//                    Function { e: Triple<String?, T?, MutableList<Spo?>?>? ->
//                        val left = e!!.getLeft()
//                        val middle = e.getMiddle()
//                        val right: MutableList<Spo?>? = e.getRight()
//                        Pair.of<T?, MutableList<Spo?>?>(middle, right)
//                    },
//                    BinaryOperator { o: Pair<T?, MutableList<Spo?>?>?, n: Pair<T?, MutableList<Spo?>?>? -> n })
//            )
//
//        return collect
//    }
//
//
//    fun <T> readExcelWithSpo(
//        file: File?,
//        entityClass: Class<T?>?, keyFunction: Function<T?, *>?, contextTableName: String?
//    ): Pair<MutableList<T?>?, MutableList<Spo?>?> {
//        return readExcelWithSpo(EasyExcel.read(file), entityClass, keyFunction, contextTableName)
//    }
//
//    fun <T> readExcelWithSpo(
//        excelReaderBuilder: ExcelReaderBuilder,
//        entityClass: Class<T?>,
//        keyFunction: Function<T?, *>,
//        contextTableName: String?
//    ): Pair<MutableList<T?>?, MutableList<Spo?>?>? {
//        val myHeadMap: BiMap<Int?, String?> = BiMap(HashMap<K?, V?>())
//
//        //        List<Spo> spoList = new ArrayList<>();
////        List<T> dataList = new ArrayList<>();
////                1=单位名称
////                1=dasd
//        val ret: MutableMap<String?, Any?> = HashMap<String?, Any?>()
//        val ret2: MutableMap<String?, Any?> = HashMap<String?, Any?>()
//        val retList: MutableList<T?> = ArrayList<T?>()
//        val spoRet: ArrayList<Spo?> = ArrayList<Spo?>()
//
//        val readListener: AnalysisEventListener<MutableMap<Int?, Any?>?> =
//            object : AnalysisEventListener<MutableMap<Int?, Any?>?>() {
//                public override fun extra(extra: CellExtra?, context: AnalysisContext?) {
//                    super.extra(extra, context)
//                }
//
//
//                public override fun invokeHeadMap(headMap: MutableMap<Int?, String?>?, context: AnalysisContext?) {
//                    myHeadMap.putAll(headMap)
//                }
//
//                public override fun invoke(data: MutableMap<Int?, Any?>, context: AnalysisContext?) {
//                    val tHeaderMap: BiMap<Int?, String?> = BiMap(HashMap<K?, V?>())
//
//
//                    Arrays.stream<Field>(entityClass.getDeclaredFields())
//                        .filter { field: Field? -> field!!.isAnnotationPresent(ExcelProperty::class.java) }
//                        .forEach { field: Field? ->
//                            field!!.setAccessible(true)
//                            val annotation: ExcelProperty = field.getAnnotation<T?>(ExcelProperty::class.java)
//                            val headerName: String? = annotation.value()[0]
//                            val i: Int? = myHeadMap.getInverse().get(headerName)
//                            tHeaderMap.put(i, field.getName())
//                        }
//
//                    val tIndexs: MutableSet<Int?>? = tHeaderMap.keySet()
//
//
//                    myHeadMap.entrySet().forEach({ h ->
//                        val index: Int? = h.getKey()
//                        val headerName: String? = h.getValue()
//                        val fieldName: String? = tHeaderMap.get(index)
//                        val value = data.getOrDefault(index, null)
//                        if (CollUtil.contains(tIndexs, index)) {
//                            ret.put(fieldName, value)
//                            return@forEach
//                        }
//                        ret2.put(headerName, value)
//                    })
//
//                    val t1: T? = BeanUtil.mapToBean(ret, entityClass, null)
//                    retList.add(t1)
//                    //构造spo
//                    val subject: Any? = keyFunction.apply(t1)
//                    val str1: String? = Convert.toStr(subject)
//
//                    val spos1: MutableList<Spo?> =
//                        ret2.entries.stream().map<Any?> { e: MutableMap.MutableEntry<String?, Any?>? ->
//                            val headerName = e!!.key
//                            val value = e.value
//                            val `val`: String? = Convert.toStr(value)
//                            val spo: Spo = Spo(str1, headerName, `val`, contextTableName)
//                            spo
//                        }
//                            .filter { spo: Any? -> StrUtil.isNotBlank(spo.getObject()) }
//                            .collect(Collectors.toList())
//                    spoRet.addAll(spos1)
//                }
//
//                public override fun doAfterAllAnalysed(context: AnalysisContext?) {
//                }
//            }
//        excelReaderBuilder.registerReadListener(readListener)
//        excelReaderBuilder.sheet().doRead()
//        return Pair.of<MutableList<T?>?, MutableList<Spo?>?>(retList, spoRet)
//    }
//}
