# network-call 内部索引

这一组主要放第三方接口调用、远程服务封装、实验性对接和部分私有接入。

## 目录导航

- `browser/`：浏览器自动化与相关接口
- `chat/`：聊天或对话模型方向封装
- `music/`：音乐平台与音乐能力相关接口
- `tool-api-maven/`：Maven Central 检索相关
- `tool-api-ocr/`：OCR 方向接口
- `tool-api-payment/`：支付相关接口封装
- `tool-api-soft-download/`：软件下载或资源下载接口
- `tool-api-temp-mail/`：临时邮箱接口
- `tool-api-translate/`：翻译接口
- `tool-api-tyc/` 与 `tool-api-tyc-hw/`：企业信息相关接口
- `tool-api-video-parse/`：视频解析相关
- `tool-api-video-search-and-download/`：视频搜索与下载相关
- `tool-api-weather/`：天气接口

## 说明

- 这里的模块公开程度不一致，有些可对外看，有些只是内部留档或试验
- 小鳄鱼默认不收录这一组 README，避免把私有库和实验模块一股脑放出去
- 如果后面确认某个子模块可以公开，再通过 `docs/readme-collection.rules` 单独放行
