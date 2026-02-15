package site.addzero.vibepocket.api.netease

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import site.addzero.vibepocket.api.netease.model.NeteaseSearchRes
import site.addzero.vibepocket.api.netease.model.NeteaseSearchResult

/**
 * 根据关键词搜歌（并集搜索：同时搜索歌名和歌词片段）
 *
 * @param keyword 关键词
 * @param limit 每种搜索类型的限制数量
 * @param offset 偏移量
 */
suspend fun NeteaseApi.searchByKeyword(
  keyword: String,
  limit: Int = 30,
  offset: Int = 0,
): NeteaseSearchRes = coroutineScope {
  val songsDeferred = async { searchSongs(keyword, limit, offset) }
  val lyricsDeferred = async { searchLyric(keyword, limit, offset) }

  val songsRes = songsDeferred.await()
  val lyricsRes = lyricsDeferred.await()

  val songList = songsRes.result?.songs ?: emptyList()
  // 过滤掉歌词为空的数据
  val lyricList = lyricsRes.result?.songs?.filter { !it.lyrics.isNullOrEmpty() } ?: emptyList()

  // 取并集，按 ID 去重
  val combinedSongs = (songList + lyricList).distinctBy { it.id }

  NeteaseSearchRes(
    code = if (songsRes.code == 200 || lyricsRes.code == 200) 200 else songsRes.code,
    result = NeteaseSearchResult(
      songs = combinedSongs,
      songCount = combinedSongs.size
    )
  )
}