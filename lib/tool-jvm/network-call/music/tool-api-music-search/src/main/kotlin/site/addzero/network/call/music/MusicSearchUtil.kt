package site.addzero.network.call.music

import site.addzero.network.call.music.model.*

/**
 * 音乐搜索工具类
 * 提供简化的静态方法
 */
object MusicSearchUtil {
    
    private val client = MusicSearchClient()
    
    /**
     * 搜索歌曲
     * 
     * @param keywords 搜索关键词
     * @param limit 返回数量，默认 30
     * @return 歌曲列表
     */
    fun searchSongs(keywords: String, limit: Int = 30): List<Song> {
        return client.searchSongs(keywords, limit)
    }
    
    /**
     * 搜索歌手
     * 
     * @param keywords 搜索关键词
     * @param limit 返回数量，默认 30
     * @return 歌手列表
     */
    fun searchArtists(keywords: String, limit: Int = 30): List<Artist> {
        return client.searchArtists(keywords, limit)
    }
    
    /**
     * 搜索专辑
     * 
     * @param keywords 搜索关键词
     * @param limit 返回数量，默认 30
     * @return 专辑列表
     */
    fun searchAlbums(keywords: String, limit: Int = 30): List<Album> {
        return client.searchAlbums(keywords, limit)
    }
    
    /**
     * 搜索歌单
     * 
     * @param keywords 搜索关键词
     * @param limit 返回数量，默认 30
     * @return 歌单列表
     */
    fun searchPlaylists(keywords: String, limit: Int = 30): List<Playlist> {
        return client.searchPlaylists(keywords, limit)
    }
    
    /**
     * 根据歌名和歌手搜索
     * 
     * @param songName 歌名
     * @param artistName 歌手名（可选）
     * @return 匹配的歌曲列表
     */
    fun searchBySongAndArtist(songName: String, artistName: String? = null): List<Song> {
        return client.searchBySongAndArtist(songName, artistName)
    }
    
    /**
     * 根据歌词搜索
     * 
     * @param lyricFragment 歌词片段
     * @return 匹配的歌曲列表
     */
    fun searchByLyric(lyricFragment: String): List<Song> {
        return client.searchByLyric(lyricFragment)
    }
    
    /**
     * 获取歌词
     * 
     * @param songId 歌曲 ID
     * @return 歌词信息
     */
    fun getLyric(songId: Long): LyricResponse {
        return client.getLyric(songId)
    }
    
    /**
     * 根据歌名获取歌词
     * 
     * @param songName 歌名
     * @param artistName 歌手名（可选，用于精确匹配）
     * @return 歌词信息，如果找不到返回 null
     */
    fun getLyric(songName: String, artistName: String? = null): LyricResponse? {
        return client.getLyricBySongName(songName, artistName)
    }
    
    /**
     * 根据歌词片段获取完整歌词
     * 
     * @param lyricFragment 歌词片段
     * @param limit 返回数量限制，默认 5
     * @param filterEmpty 是否过滤空歌词，默认 true
     * @return 歌曲与歌词组合列表
     */
    fun getLyricsByFragment(lyricFragment: String, limit: Int = 5, filterEmpty: Boolean = true): List<SongWithLyric> {
        return client.getLyricsByFragment(lyricFragment, limit, filterEmpty)
    }
    
    /**
     * 获取歌曲详情
     * 
     * @param songId 歌曲 ID
     * @return 歌曲详情
     */
    fun getSongDetail(songId: Long): Song? {
        return client.getSongDetail(listOf(songId)).firstOrNull()
    }
    
    /**
     * 批量获取歌曲详情
     * 
     * @param songIds 歌曲 ID 列表
     * @return 歌曲详情列表
     */
    fun getSongDetails(songIds: List<Long>): List<Song> {
        return client.getSongDetail(songIds)
    }
    
    /**
     * 搜索并获取完整信息（包含歌词）
     * 
     * @param songName 歌名
     * @param artistName 歌手名（可选）
     * @param filterEmpty 是否过滤空歌词，默认 true
     * @return 歌曲和歌词的组合列表
     */
    fun searchWithLyrics(songName: String, artistName: String? = null, filterEmpty: Boolean = true): List<SongWithLyric> {
        val songs = searchBySongAndArtist(songName, artistName)
        return songs.mapNotNull { song ->
            try {
                val lyric = getLyric(song.id)
                // 如果开启过滤且歌词为空，则跳过
                if (filterEmpty && lyric.lrc?.lyric.isNullOrBlank()) {
                    null
                } else {
                    SongWithLyric(song, lyric)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
