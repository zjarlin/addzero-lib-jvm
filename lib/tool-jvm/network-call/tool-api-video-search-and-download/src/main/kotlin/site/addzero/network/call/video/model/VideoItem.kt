package site.addzero.network.call.video.model


enum class VideoType(val displayName: String) {
  MOVIE("电影"),
  TV_SERIES("电视剧"),
  ANIME("动漫"),
  VARIETY("综艺"),
  DOCUMENTARY("纪录片"),
  UNKNOWN("未知")
}

enum class VideoPlatform(val displayName: String, string: String) {
  QQ("腾讯视频", "v.qq.com"),
  IQIYI("爱奇艺", "iqiyi.com"),
  YOUKU("优酷", "youku.com"),
  MGTV("芒果TV", "mgtv.com"),
  BILIBILI("哔哩哔哩", "bilibili.com"),
  LETV("乐视", "le.com"),
  TUDOU("土豆", "tudou.com"),
  SOHU("搜狐视频", "sohu.com"),
  M1905("1905电影网", "1905.com"),
  PPTV("PPTV", "pptv.com"),
  WASU("华数TV", "wasu.cn"),
  ACFUN("AcFun", "acfun.cn")
}
