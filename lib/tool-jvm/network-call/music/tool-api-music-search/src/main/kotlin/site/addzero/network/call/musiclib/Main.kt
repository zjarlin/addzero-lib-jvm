package site.addzero.network.call.musiclib

import site.addzero.network.call.musiclib.utils.HttpClientManager
import kotlinx.coroutines.runBlocking

/**
 * 示例入口函数
 */
fun main() = runBlocking {
    try {
        // 示例：使用网易云搜索
        println("=== 网易云音乐搜索示例 ===")
        val netease = MusicAPIFactory.create("netease")
        val neteaseResults = netease.search("周杰伦")
        neteaseResults.take(3).forEach { song ->
            println("${song.name} - ${song.artist} (${song.formatDuration()})")
        }

        // 示例：使用QQ音乐搜索
        println("\n=== QQ音乐搜索示例 ===")
        val qq = MusicAPIFactory.create("qq")
        val qqResults = qq.search("周杰伦")
        qqResults.take(3).forEach { song ->
            println("${song.name} - ${song.artist} (${song.formatDuration()})")
        }

        // 示例：使用酷狗搜索
        println("\n=== 酷狗音乐搜索示例 ===")
        val kugou = MusicAPIFactory.create("kugou")
        val kugouResults = kugou.search("周杰伦")
        kugouResults.take(3).forEach { song ->
            println("${song.name} - ${song.artist} (${song.formatDuration()})")
        }

        // 示例：获取Bilibili视频音频
        println("\n=== Bilibili 搜索示例 ===")
        val bilibili = MusicAPIFactory.create("bilibili")
        val biliResults = bilibili.search("音乐")
        biliResults.take(3).forEach { song ->
            println("${song.name} - ${song.artist} (${song.formatDuration()})")
        }

        println("\n所有示例执行完成!")

    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        HttpClientManager.close()
    }
}
