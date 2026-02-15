//import site.addzero.vibepocket.api.music.*
//import site.addzero.vibepocket.api.netease.*
//import site.addzero.vibepocket.api.netease.model.MusicSearchType
//
///**
// * 网易云音乐 [MusicService] 实现
// */
//object NeteaseMusicService : MusicService {
//
//  override val platformId: String = "netease"
//
//  override suspend fun searchSongs(keyword: String, limit: Int, offset: Int): List<MusicTrack> {
//  MusicSearchClient.musicApi.search(
//      s = TODO(),
//      type = MusicSearchType.SONG.value
//      limit = TODO(),
//      offset = TODO(),
//    )
//    val resp = api.searchSongs(keyword, limit = limit, offset = offset)
//    return resp.result?.songs?.map { it.toMusicTrack() } ?: emptyList()
//  }
//
//  override suspend fun getDetail(trackId: String): MusicTrack? {
//    TODO("Not yet implemented")
//  }
//
//  override suspend fun getLyric(trackId: String): MusicLyric? {
//    TODO("Not yet implemented")
//  }
//
//}
