package com.gisroad.business.util.easyexcel

import cn.idev.excel.context.AnalysisContext
import cn.idev.excel.event.AnalysisEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * excel解析过程监听器
 *
 * @author liangxn
 */
class DataListener<T> : AnalysisEventListener<T?>() {
    val rows: MutableList<T?> = ArrayList<T?>()

    public override fun invoke(t: T?, analysisContext: AnalysisContext?) {
        rows.add(t)
    }

    public override fun doAfterAllAnalysed(context: AnalysisContext?) {
        LOGGER.info("解析完成！读取{}行", rows.size)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DataListener::class.java)
    }
}
