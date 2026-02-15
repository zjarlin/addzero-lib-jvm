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
class TestNeteaseApi {

  /** 通用搜索（按 type 区分搜索类型） */
  @SemanticVariation(name = "searchSongs", args = ["type=MusicSearchType.SONG.value"], doc = "搜索歌曲")
  @SemanticVariation(name = "searchAlbums", args = ["type=MusicSearchType.ALBUM.value"], doc = "搜索专辑")
  fun <T:Any,E:Throwable, R> search(
    @Query s: String,
    @Query type: Int = MusicSearchType.SONG.value,
    @Query limit: Int = 30,
    @Query offset: Int = 0,
  ): NeteaseSearchRes {
    return NeteaseSearchRes()
  }

}
