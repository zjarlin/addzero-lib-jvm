package site.addzero.network.call.suno

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import site.addzero.network.call.suno.model.SunoMusicRequest

/**
 * Suno 客户端测试
 */
@DisplayName("Suno 客户端测试")
class SunoClientTest {

    private lateinit var client: SunoClient

    @BeforeEach
    fun setup() {
        // API Token is still required by the client constructor, but its value won't be used for calls to mockWebServer
        client = SunoClient(System.getenv("SUNO_API_TOKEN"))
    }

//    @AfterEach
//    fun teardown() { }
  @Test
  fun `查询生成任务`() {
  val fetchTask = client.fetchTask("ssss")
  println()

}

    @Test
    @DisplayName("测试生成音乐")
    @Tag("unit")
    fun testGenerateMusic_success() {
        // Given
        val expectedTaskId = "task_test_id_123"
        val mockResponseJson = """
            {
              "code": 200,
              "message": "success",
              "data": "$expectedTaskId"
            }
        """.trimIndent()

        val request = SunoMusicRequest(
            title = "suno v5 阿刁翻唱(原唱张韶涵)",
            prompt = "帮我翻唱张韶涵的阿刁\n" +
              "要求如下\n" +
              "Powerful Black male gospel lead vocal,\n" +
              "deep, soulful, and resonant,\n" +
              "emotional delivery with spiritual intensity,\n" +
              "rich gospel-style backing vocals and call-and-response harmonies.\n" +
              "\n" +
              "Intro:\n" +
              "ethereal and cinematic opening,\n" +
              "air pads, distant choir textures,\n" +
              "minimalist, spacious, sacred atmosphere,\n" +
              "slow and reverent, almost ritualistic.\n" +
              "\n" +
              "Verse:\n" +
              "gradual entrance of rhythm,\n" +
              "soft percussive pulses and subtle drum effects,\n" +
              "heartbeat-like groove,\n" +
              "sense of forward motion begins,\n" +
              "vocals remain controlled and expressive.\n" +
              "\n" +
              "Pre-Chorus:\n" +
              "rhythm intensifies,\n" +
              "layered percussion, hand drums, cinematic hits,\n" +
              "marching feel develops,\n" +
              "energy steadily rising,\n" +
              "gospel harmonies becoming fuller.\n" +
              "\n" +
              "Chorus:\n" +
              "Circle of Life–inspired gospel arrangement,\n" +
              "powerful choir layers,\n" +
              "bold, uplifting, triumphant,\n" +
              "strong sense of movement and purpose,\n" +
              "emotional release with spiritual weight.\n" +
              "\n" +
              "Final Chorus:\n" +
              "driving rhythm and cinematic percussion,\n" +
              "running and chasing sensation,\n" +
              "propulsive drums, wide brass or synth swells,\n" +
              "euphori\n" +
              "\n" +
              "以下是歌词------\n" +
              "00:33.71]阿刁\n" +
              "[00:36.31]住在西藏的某个地方\n" +
              "[00:42.58]秃鹫一样\n" +
              "[00:46.15]栖息在山顶上\n" +
              "[00:52.96]阿刁\n" +
              "[00:55.54]大昭寺门前\n" +
              "[00:57.61]铺满阳光\n" +
              "[01:01.54]打一壶甜茶\n" +
              "[01:05.42]我们聊着过往\n" +
              "[01:31.25]阿刁\n" +
              "[01:33.88]你总把自己\n" +
              "[01:36.47]打扮得像   男孩子一样\n" +
              "[01:43.82]可比格桑还顽强\n" +
              "[01:50.48]阿刁\n" +
              "[01:53.10]虚伪的人\n" +
              "[01:54.61]有千百种笑\n" +
              "[01:59.50]你何时下山\n" +
              "[02:02.99]记得带上卓玛刀\n" +
              "[02:08.71]灰色帽檐下\n" +
              "[02:11.11]凹陷的脸颊\n" +
              "[02:13.54]你很少说话\n" +
              "[02:15.92]简单的回答\n" +
              "[02:18.33]明天在哪里\n" +
              "[02:20.65]谁会在意你\n" +
              "[02:23.82]即使倒在路上\n" +
              "[02:29.44]接受 放逐\n" +
              "[02:31.16]困惑 自由\n" +
              "[02:32.92]就像风一样\n" +
              "[02:35.64]吹过坎坷\n" +
              "[02:37.36]不平的路途\n" +
              "[02:38.74]漫漫的脚步婆娑\n" +
              "[02:41.61]慢慢的足迹斑驳\n" +
              "[02:43.47]或者 连一丝痕迹\n" +
              "[02:45.63]都不 留在这里\n" +
              "[02:48.36]可我 还是不会\n" +
              "[02:50.69]因为痛就放弃希望\n" +
              "[02:53.90]受过的伤长成疤\n" +
              "[02:55.15]开出无比美丽的花\n" +
              "[02:57.92]受过的伤长成疤\n" +
              "[03:00.13]开出无比美丽的花\n" +
              "[03:07.27]阿刁\n" +
              "[03:09.93]明天是否能吃顿饱饭\n" +
              "[03:16.16]你已习惯\n" +
              "[03:19.80]孤独是一种信仰\n" +
              "[03:26.47]阿刁\n" +
              "[03:29.11]不会被现实磨平棱角\n" +
              "[03:35.97]你不是这世界的人呐\n" +
              "[03:38.64]没必要在乎真相\n" +
              "[03:44.69]命运多舛\n" +
              "[03:47.47]痴迷 淡然\n" +
              "[03:49.51]挥别了青春\n" +
              "[03:51.78]数不尽的车站\n" +
              "[03:54.26]甘于平凡\n" +
              "[03:55.18]却不甘平凡地溃败\n" +
              "[03:59.36]你是阿刁\n" +
              "[04:03.10]你是自由的鸟\n" +
              "[04:23.28]命运多舛\n" +
              "[04:25.41]痴迷 淡然\n" +
              "[04:27.88]挥别了青春\n" +
              "[04:30.20]数不尽的车站\n" +
              "[04:32.63]甘于平凡\n" +
              "[04:33.59]却不甘平凡地溃败\n" +
              "[04:37.40]你是阿刁\n" +
              "[04:52.94]阿刁\n" +
              "[04:55.52]爱情是粒悲伤的种子\n" +
              "[05:01.63]你是一棵树\n" +
              "[05:05.37]你永远都不会枯\n" +
              "[05:12.25]\n" +
              "[05:16.36] 原唱：赵雷\n" +
              "[05:17.20] 编曲：孔德岳/龙隆\n" +
              "[05:18.03] 制作人：龙隆/宋予宾\n" +
              "[05:18.86] Program：孔德岳\n" +
              "[05:19.70] 伴唱：西曼\n" +
              "[05:20.53] 童声：湖南省青少年活动中心少儿艺术团\n" +
              "), tlyric=LyricContent(version=0, lyric=), romalrc=null))\n",
            makeInstrumental = false,
            mv = "chirp-v5",
            tags = "pop, 黑人福音"
        )
        // When
        val taskId = client.generateMusic(request)
        println("✓ 生成音乐任务成功，任务 ID: $taskId")
      //todo 序列化歌曲元数据和任务id到硬盘以便后续查询
    }
  //再写一个任务查询的
}
