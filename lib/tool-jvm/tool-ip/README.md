# tool-ip

本机网络地址工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-ip")
```

主要能力：

- `site.addzero.common.util.IPUtils.localIp`：获取本机可访问 IPv4 地址；优先选择真实物理网卡上的私有地址，排除回环、链路本地、隧道/虚拟网卡以及 `198.18.0.0/15` 基准测试网段。
- `site.addzero.common.util.IPUtils.localIpOrNull()`：未找到可用 IPv4 时返回 `null`，适合需要自行处理兜底的场景。
