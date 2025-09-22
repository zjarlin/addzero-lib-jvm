package graavlvm_download

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

/**
 * GraalVM下载工具类
 */
object GraalVMDOWNLOAD {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * 从GitHub下载GraalVM JDK文件
     * @param url 下载链接
     * @param destinationPath 保存路径（先写死）
     * @return 下载是否成功
     */
    fun downloadGraalVMJDK(url: String, destinationPath: String = "/tmp/graalvm-jdk.zip"): Boolean {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("authority", "github.com")
                .header("accept", "text/html, application/xhtml+xml")
                .header("accept-language", "zh-CN,zh;q=0.9")
                .header("referer", "https://github.com/graalvm/graalvm-ce-builds/releases")
                .header("sec-ch-ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "same-origin")
                .header("turbo-visit", "true")
                .header(
                    "user-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                )
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("下载失败，HTTP状态码: ${response.code}")
                    return false
                }
                
                val body: ResponseBody = response.body ?: run {
                    println("响应体为空")
                    return false
                }
                
                // 创建目标目录
                val destinationFile = File(destinationPath)
                destinationFile.parentFile?.let { parentDir ->
                    if (!parentDir.exists()) {
                        parentDir.mkdirs()
                    }
                }
                
                // 写入文件并显示进度
                saveToFileWithProgress(body, destinationFile)
                println("文件下载成功，保存路径: $destinationPath")
                true
            }
        } catch (e: Exception) {
            println("下载过程中发生错误: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 将响应体保存到文件并显示进度
     * @param body 响应体
     * @param file 目标文件
     */
    private fun saveToFileWithProgress(body: ResponseBody, file: File) {
        val fileSize = body.contentLength()
        var totalBytesRead = 0L
        val buffer = ByteArray(8192)
        
        FileOutputStream(file).use { outputStream ->
            body.byteStream().use { inputStream ->
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    
                    // 显示进度
                    if (fileSize > 0) {
                        val progress = (totalBytesRead * 100 / fileSize).toInt()
                        print("\r下载进度: $progress% ($totalBytesRead / $fileSize bytes)")
                    }
                }
                outputStream.flush()
            }
        }
        println() // 换行
    }
    
    /**
     * 主函数，用于测试下载功能
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val url = "https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-25.0.0/graalvm-community-jdk-25.0.0_windows-x64_bin.zip"
        val destinationPath = "/tmp/graalvm-jdk.zip" // 先写死路径
        
        println("开始下载GraalVM JDK...")
        val success = downloadGraalVMJDK(url, destinationPath)
        
        if (success) {
            println("下载完成!")
        } else {
            println("下载失败!")
        }
    }
}