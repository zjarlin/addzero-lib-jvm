package site.addzero.web.infra.upload

import cn.hutool.core.codec.Base64Encoder
import cn.hutool.core.util.StrUtil
import site.addzero.web.infra.constant.ContentTypeEnum
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.OutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * @author zjarlin
 * @since 2023/11/28 10:25
 */
object DownloadUtil {



    /**
     * 调用浏览器文件下载
     */
    fun downloadExcel(fileName: String, consumer: Consumer<OutputStream>) {
        download(fileName, consumer, ContentTypeEnum.XLSX)
    }

    fun download(fileName: String, consumer: Consumer<OutputStream>, tab: ContentTypeEnum) {
        download(fileName, consumer, tab, true)
    }

    fun download(fileName: String, consumer: Consumer<OutputStream>, tab: ContentTypeEnum, addPostfix: Boolean) {
        val application: String = tab.application
        val postfix: String = tab.postfix
        val response = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).response
        response?.characterEncoding = "UTF-8"
        //得请求头中的User-Agent
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val agent: String = request.getHeader("User-Agent")

        // 根据不同的客户端进行不同的编码
        var filenameEncoder = ""
        if (agent.contains("MSIE")) {
            // IE浏览器
            filenameEncoder = URLEncoder.encode(fileName, "utf-8")
            filenameEncoder = filenameEncoder.replace("+", " ")
        } else if (agent.contains("Firefox")) {
            // 火狐浏览器
            val encode = Base64Encoder.encode(fileName.toByteArray(StandardCharsets.UTF_8))
            filenameEncoder = "=utf-8B$encode="
        } else {
            // 其它浏览器
            filenameEncoder = URLEncoder.encode(fileName, "utf-8")
        }
        if (addPostfix) {
            filenameEncoder = StrUtil.addSuffixIfNot(filenameEncoder, postfix)
        }
        //        filenameEncoder = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        response?.setHeader(
            "Content-disposition", "attachment;filename=$filenameEncoder"
        )
        response?.contentType = application
        val outputStream: OutputStream = response?.outputStream!!
        consumer.accept(outputStream)
    }


    fun downloadZip(fileName: String, consumer: Consumer<OutputStream>) {
        download(fileName, consumer, ContentTypeEnum.ZIP)
    }
}