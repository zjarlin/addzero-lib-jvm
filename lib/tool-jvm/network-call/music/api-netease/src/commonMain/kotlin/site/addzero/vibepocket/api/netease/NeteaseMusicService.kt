package site.addzero.vibepocket.api.netease//package site.addzero.vibepocket.api.netease
//
//import site.addzero.vibepocket.api.music.MusicLyric
//import site.addzero.vibepocket.api.music.MusicService
//import site.addzero.vibepocket.api.music.MusicTrack
//
///**
// * 网易云音乐 [MusicService] 实现
// */
//class NeteaseMusicService(
//    private val api: NeteaseApi = MusicSearchClient.musicApi,
//) : MusicService {
//
//    override val platformId: String = "netease"
//
//    override suspend fun searchSongs(keyword: String, limit: Int, offset: Int): List<MusicTrack> {
//        val resp = api.searchSongs(keyword, limit = limit, offset = offset)
//        return resp.result?.songs?.map { it.toMusicTrack() } ?: emptyList()
//    }
//
//    override suspend fun getTrackDetail(trackId: String): MusicTrack? {
//        // 网易云公开 API 没有单曲详情接口，用搜索 + 过滤模拟
//        val resp = api.searchSongs(trackId, limit = 5)
//        return resp.result?.songs
//            ?.firstOrNull { it.id.toString() == trackId }
//            ?.toMusicTrack()
//    }
//
//    override suspend fun getLyric(trackId: String): MusicLyric? {
//        val id = trackId.toLongOrNull() ?: return null
//        val resp = api.getLyric(id)
//        val lrc = resp.lrc?.lyric ?: return null
//        return MusicLyric(
//            lrc = lrc,
//            translatedLrc = resp.tlyric?.lyric?.takeIf { it.isNotBlank() },
//        )
//    }
//
//    private fun NeteaseSearchSong.toMusicTrack() = MusicTrack(
//        id = id.toString(),
//        name = name,
//        artist = artistNames,
//        album = album?.name ?: "",
//        coverUrl = coverUrl,
//        durationMs = duration,
//        platform = platformId,
//    )
//}
