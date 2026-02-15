package site.addzero.vibepocket.api.netease

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import site.addzero.ksp.metadata.semantic.annotations.SemanticVariation
import site.addzero.vibepocket.api.netease.model.LyricRes
import site.addzero.vibepocket.api.netease.model.MusicSearchType
import site.addzero.vibepocket.api.netease.model.NeteaseSearchRes
import site.addzero.vibepocket.api.netease.model.SongDetailResponse

/**
 * 网易云音乐 API 接口定义（Ktorfit 声明式）
 *
 * 纯 HTTP 接口声明，不含业务逻辑。
 * type: 1=歌曲, 10=专辑, 100=歌手, 1000=歌单, 1006=歌词
 */
interface NeteaseApi {

  /** 通用搜索（按 type 区分搜索类型） */
  @GET("search/get/web")
//  @SemanticVariation(name = "searchSongs", args = ["type=site.addzero.vibepocket.api.netease.model.MusicSearchType.SONG.value"], doc = "搜索歌曲")
//  @SemanticVariation(name = "searchAlbums", args = ["type=site.addzero.vibepocket.api.netease.model.MusicSearchType.ALBUM.value"], doc = "搜索专辑")
  suspend fun search(
    @Query s: String,
    @Query type: Int = MusicSearchType.SONG.value,
    @Query limit: Int = 30,
    @Query offset: Int = 0,
  ): NeteaseSearchRes

  /** 获取歌词 */
  @GET("song/lyric")
  suspend fun getLyric(
    @Query id: Long,
    @Query lv: Int = 1,
    @Query tv: Int = 1,
  ): LyricRes

  /** 获取歌曲详情 */
  @GET("song/detail")
  suspend fun getSongDetail(
    @Query("ids") ids: String,
//    @Header("User-Agent") userAgent: String = UA,
//    @Header("Referer") referer: String = REFERER,
  ): SongDetailResponse

//  companion object {
//    const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
//    const val REFERER = "https://music.163.com/"
//  }
}
