//package com.gisroad.business.util.easyexcel
//
//import cn.hutool.core.collection.CollUtil
//import cn.hutool.core.io.file.PathUtil
//import cn.hutool.core.io.resource.ClassPathResource
//import cn.hutool.core.util.IdUtil
//import cn.idev.excel.EasyExcel
//import cn.idev.excel.enums.WriteDirectionEnum
//import cn.idev.excel.write.builder.ExcelWriterSheetBuilder
//import cn.idev.excel.write.merge.OnceAbsoluteMergeStrategy
//import cn.idev.excel.write.metadata.WriteSheet
//import cn.idev.excel.write.metadata.fill.FillConfig
//import cn.idev.excel.write.style.HorizontalCellStyleStrategy
//import com.gisroad.business.util.easyexcel.EasyExcelWriteUtil.Companion.writeExcel
//import com.gisroad.business.util.easyexcel.converter.jodatime.LocalDateConverter
//import com.gisroad.business.util.easyexcel.converter.jodatime.LocalDateTimeConverter
//import com.gisroad.business.util.easyexcel.converter.jodatime.LocalTimeConverter
//import com.gisroad.business.util.easyexcel.entity.ExportSheetConfig
//import com.gisroad.business.util.easyexcel.find_merge_range.MergeRangeFinder
//import com.gisroad.business.util.easyexcel.find_merge_range.Range
//import com.gisroad.business.util.easyexcel.strategy.CustomCellStyleStrategy
//import org.apache.poi.ss.usermodel.Workbook
//import java.io.File
//import java.io.OutputStream
//import java.util.*
//import java.util.stream.Collectors
//import java.util.stream.Stream
//import javax.servlet.ServletOutputStream
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse
//
///**
// * easyexcel写工具类
// *
// * @author zjarlin
// * @since 2023/04/30
// */
//class EasyExcelWriteUtil {
//
//
//    companion object {
//        private var response: HttpServletResponse? = null
//        private var httpServletRequest: HttpServletRequest? = null
//
//        /**
//         * 模板填充
//         *
//         * @param response     响应
//         * @param vo           签证官
//         * @param templatePath 模板路径
//         * @param fileName     文件名称 入参
//         * @author addzero
//         * @since 2022/11/27
//         */
//        fun <VO> templateFill(
//            response: HttpServletResponse,
//            vo: VO?,
//            templatePath: String?,
//            fileName: String?
//        ) {
//            templateFill<VO?, Any?>(response, vo, null, templatePath, fileName)
//        }
//
//        /**
//         * 模板填充
//         *
//         * @param response     响应
//         * @param vo           一维字段填充
//         * @param dtos         dto 二维列表填充
//         * @param templatePath 模板路径
//         * @param fileName     文件名称 入参
//         * @author addzero
//         * @since 2022/11/27
//         */
//        fun <VO, DTO> templateFill(
//            response: HttpServletResponse,
//            vo: VO?,
//            dtos: MutableList<DTO?>?,
//            templatePath: String?,
//            fileName: String?
//        ) {
//            val out: ServletOutputStream = response.getOutputStream()
//            response.setContentType("multipart/form-data")
//            response.setCharacterEncoding("UTF-8")
//
//            response.setHeader("Content-disposition", "attachment;filename=" + fileName)
//            //文件模板输入流
//            val inputStream = ClassPathResource(templatePath).stream
//
//            val writer = EasyExcel.write(out)
//                .withTemplate(inputStream) //.registerConverter(new LocalDateConverter())
//                //.registerConverter(new LocalDateTimeConverter())
//                .build()
//
//            //3.4 设置强制计算公式：不然公式会以字符串的形式显示在excel中
//            val workbook: Workbook = writer.writeContext().writeWorkbookHolder().getWorkbook()
//            workbook.setForceFormulaRecalculation(true)
//
//            val sheet: WriteSheet? = EasyExcel.writerSheet(0).build()
//            //填充列表开启自动换行,自动换行表示每次写入一条list数据是都会重新生成一行空行,此选项默认是关闭的,需要提前设置为true
//            val fillConfig: FillConfig? = FillConfig.builder()
//                .forceNewRow(true)
//                .direction(WriteDirectionEnum.VERTICAL)
//                .build()
//            //填充一维数据
//            writer.fill(vo, sheet)
//            //填充二维数据
//            if (CollUtil.isNotEmpty(dtos)) {
//                writer.fill(dtos, fillConfig, sheet)
//            }
//            //填充完成
//            writer.finish()
//            out.flush()
//        }
//
//        /**
//         * 列表导出下载
//         *
//         * @param list     列表
//         * @param fileName 文件名称 入参
//         * @author addzero
//         * @since 2022/11/27
//         */
//        fun <T> listExport(list: MutableList<T?>, fileName: String?) {
//            val sheetConfig = object : ExportSheetConfig() {
//                init {
//                    data = list
//                    sheetName = "sheet1"
//                }
//            }
//
//            DownloadUtil.downloadExcel(fileName, { outputStream -> writeExcel(outputStream, sheetConfig) })
//        }
//
//
//        fun <T> writeExcel(filePath: String?, vararg exportConfig: ExportSheetConfig) {
//            // 如果这里想使用03 则 传入excelType参数即可
//            @Cleanup val excelWriter: ExcelWriter = EasyExcel.write(filePath)
//                .build()
//            Companion.writeExcel(excelWriter, exportConfig)
//        }
//
//        fun <T> writeExcel(outputStream: OutputStream?, vararg exportConfig: ExportSheetConfig) {
//            // 如果这里想使用03 则 传入excelType参数即可
//            @Cleanup val excelWriter: ExcelWriter = EasyExcel.write(outputStream).build()
//            Companion.writeExcel(excelWriter, exportConfig)
//        }
//
//        fun <T> writeExcel(file: File?, vararg exportConfig: ExportSheetConfig) {
//            // 如果这里想使用03 则 传入excelType参数即可
//            @Cleanup val excelWriter: ExcelWriter = EasyExcel.write(file).build()
//            Companion.writeExcel(excelWriter, exportConfig)
//        }
//
//        private fun writeExcel(
//            excelWriter: ExcelWriter, exportSheetConfigs: Array<ExportSheetConfig>
//        ) {
//            Stream.iterate<Int?>(0) { i: Int? ->
//                var i = i
//                i = i!! + 1
//            }.limit(exportSheetConfigs.size.toLong()).forEach { i: Int? ->
//                val exportSheetConfig: ExportSheetConfig = exportSheetConfigs[i!!]
//                val sheetName: String? = exportSheetConfigs[i].getSheetName()
//                val automaticallyMergeCells: Boolean = exportSheetConfig.getAutomaticallyMergeCells()
//                val data: MutableList<*> = exportSheetConfig.getData()
//                if (CollUtil.isNotEmpty(data)) {
//                    val aClass: Class<*> = data.get(0)!!.javaClass
//
//                    val excelWriterSheetBuilder: ExcelWriterSheetBuilder = EasyExcel.writerSheet(i, sheetName)
//                        .useDefaultStyle(exportSheetConfig.getUseDefaultStyle()) // 取消导出Excel的默认风格
//                        //自动清理空值
//                        .autoTrim(exportSheetConfig.getAutoTrim()) //自动合并表头
//                        .automaticMergeHead(exportSheetConfig.getAutomaticMergeHead())
//                        .registerConverter(LocalDateConverter())
//                        .registerConverter(LocalDateTimeConverter())
//                        .registerConverter(LocalTimeConverter())
//
//                        .head(aClass)
//
//                    if (automaticallyMergeCells) {
//                        /**
//                         * 自动合并单元格策略
//                         */
//                        buildAutoMerge(data, excelWriterSheetBuilder)
//                    }
//                    /**  自适应列宽 */
//                    if (exportSheetConfig.getAdaptiveColumnWidth()) {
//                        excelWriterSheetBuilder.registerWriteHandler(ExcelWidthStyleStrategy())
//                    }
//                    if (exportSheetConfig.getIsCenter()) {
//                        /**  自定义样式单元格水平垂直居中 */
//                        excelWriterSheetBuilder.registerWriteHandler(CustomCellStyleStrategy())
//                    }
//                    val writeSheet: WriteSheet? = excelWriterSheetBuilder.build()
//                    excelWriter.write(data, writeSheet)
//                }
//            }
//        }
//
//        /**
//         * 自动合并单元格策略
//         */
//        fun buildAutoMerge(data: MutableList<*>?, excelWriterSheetBuilder: ExcelWriterSheetBuilder) {
//            //自动合并策略
//            val onceAbsoluteMergeStrategy: MutableList<OnceAbsoluteMergeStrategy?> =
//                Companion.getOnceAbsoluteMergeStrategy(data)
//            if (CollUtil.isNotEmpty(onceAbsoluteMergeStrategy)) {
//                onceAbsoluteMergeStrategy.forEach(excelWriterSheetBuilder::registerWriteHandler)
//            }
//
//            excelWriterSheetBuilder.registerWriteHandler(HorizontalCellStyleStrategy())
//        }
//
//        fun <T> writeExcel(filePath: String?, data: MutableList<*>) {
//            writeExcel(filePath, data, null)
//        }
//
//        fun <T> writeExcel(filePath: String?, data: MutableList<T?>, tClass: Class<T?>?) {
//            if (CollUtil.isEmpty(data) && Objects.nonNull(tClass)) {
//                val t: T? = ReflectUtil.newInstance(tClass)
//                data.add(t)
//            }
//            Companion.writeExcel<Any?>(filePath, "sheet1", data)
//        }
//
//        fun <T> writeExcel(filePath: String?, sheetName: String?, data: MutableList<*>?) {
//            val exportSheetConfig: ExportSheetConfig = object : ExportSheetConfig() {
//                init {
//                    setSheetName(sheetName)
//                    setData(data)
//                }
//            }
//            Companion.writeExcel<Any?>(filePath, exportSheetConfig)
//        }
//
//        fun <T> getOnceAbsoluteMergeStrategy(list: MutableList<T?>?): MutableList<OnceAbsoluteMergeStrategy?> {
//            val mergeRanges: MutableList<Range?> = MergeRangeFinder.findMergeRanges<T?>(list)
//            val collect: MutableList<OnceAbsoluteMergeStrategy?> = mergeRanges.stream().map<Any?> { e: Range? ->
//                OnceAbsoluteMergeStrategy(
//                    e.getStartRow(),
//                    e.getEndRow(),
//                    e.getStartCol(),
//                    e.getEndCol()
//                )
//            }.collect(
//                Collectors.toList()
//            )
//            return collect
//        }
//
//        fun exportMap(data: MutableList<MutableMap<String?, Any?>?>): File? {
//            val header = data.stream()
//                .flatMap<String?> { e: MutableMap<String?, Any?>? -> e!!.keys.stream() }
//                .distinct()
//                .collect(Collectors.toList())
//
//            return exportMap(data, header)
//        }
//
//        fun exportMap(data: MutableList<MutableMap<String?, Any?>?>, header: MutableList<String?>): File? {
//            return exportMap(data, header, null)
//        }
//
//        fun exportMap(data: MutableList<MutableMap<String?, Any?>?>, header: MutableList<String?>, dir: File?): File? {
//            val excelHeader = header.stream()
//                .map<MutableList<String?>> { e: String? -> Stream.of<String?>(e).collect(Collectors.toList()) }
//                .collect(Collectors.toList())
//
//            val excelData = data.stream()
//                .map<MutableList<Any?>> { e: MutableMap<String?, Any?>? ->
//                    header.stream()
//                        .map<Any?> { col: String? -> e!!.getOrDefault(col, "") }
//                        .collect(Collectors.toList())
//                }
//                .collect(Collectors.toList())
//
//            val tempFile: File? = PathUtil.createTempFile(
//                IdUtil.getSnowflakeNextIdStr(),
//                ".xlsx",
//                if (dir != null) dir.toPath() else null
//            ).toFile().getCanonicalFile()
//
//            EasyExcel.write(tempFile)
//                .head(excelHeader)
//                .sheet("Sheet1")
//                .registerWriteHandler(ExcelWidthStyleStrategy())
//                .registerWriteHandler(CustomCellStyleStrategy())
//                .doWrite(excelData)
//            return tempFile
//        }
//    }
//}
