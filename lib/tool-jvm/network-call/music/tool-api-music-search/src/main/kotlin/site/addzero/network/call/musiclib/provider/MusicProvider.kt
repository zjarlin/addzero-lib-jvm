package site.addzero.network.call.musiclib.provider

import site.addzero.network.call.musiclib.model.Playlist
import site.addzero.network.call.musiclib.model.PlaylistDetail
import site.addzero.network.call.musiclib.model.Song

/**
 * 音乐提供者接口 - 定义所有音乐源必须实现的方法
 */
interface MusicProvider {
    /**
     * 搜索歌曲
     * @param keyword 搜索关键词
     * @return 歌曲列表
     */
    suspend fun search(keyword: String): List<Song>

    /**
     * 搜索歌单
     * @param keyword 搜索关键词
     * @return 歌单列表
     */
    suspend fun searchPlaylist(keyword: String): List<Playlist>

    /**
     * 获取歌单歌曲
     * @param id 歌单ID
     * @return 歌曲列表
     */
    suspend fun getPlaylistSongs(id: String): List<Song>

    /**
     * 解析歌单链接
     * @param link 歌单链接
     * @return 歌单详情（包含歌单元数据和歌曲列表）
     */
    suspend fun parsePlaylist(link: String): PlaylistDetail

    /**
     * 解析歌曲链接
     * @param link 歌曲链接
     * @return 歌曲详情（包含下载链接）
     */
    suspend fun parse(link: String): Song

    /**
     * 获取下载链接
     * @param song 歌曲对象
     * @return 下载链接
     */
    suspend fun getDownloadURL(song: Song): String

    /**
     * 获取歌词
     * @param song 歌曲对象
     * @return 歌词文本 (LRC格式)
     */
    suspend fun getLyrics(song: Song): String

    /**
     * 获取推荐歌单
     * @return 歌单列表
     */
    suspend fun getRecommendedPlaylists(): List<Playlist> {
        throw NotImplementedError("该平台不支持获取推荐歌单")
    }

    /**
     * 检测当前账号是否为VIP
     * @return 是否为VIP
     */
    suspend fun isVipAccount(): Boolean {
        return false
    }

    /**
     * 提供者名称
     */
    val name: String

    /**
     * 提供者标识
     */
    val source: String
}

/**
 * 带Cookie的音乐提供者（需要登录的API）
 */
interface CookieMusicProvider : MusicProvider {
    var cookie: String
}
