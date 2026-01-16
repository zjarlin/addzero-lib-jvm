#!/bin/bash

echo "=== 音乐搜索 API 简单测试 ==="
echo ""

# 测试网易云音乐 API 是否可访问
echo "测试 1: 检查 API 可访问性"
response=$(curl -s -w "\n%{http_code}" "https://music.163.com/api/search/get/web?s=%E6%99%B4%E5%A4%A9&type=1&limit=1&offset=0" \
  -H "User-Agent: Mozilla/5.0" \
  -H "Referer: https://music.163.com/")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | head -n-1)

if [ "$http_code" = "200" ]; then
    echo "✓ API 可访问 (HTTP $http_code)"
    echo "响应示例:"
    echo "$body" | head -c 200
    echo "..."
else
    echo "✗ API 不可访问 (HTTP $http_code)"
fi

echo ""
echo "测试 2: 搜索歌曲 - 晴天"
response=$(curl -s "https://music.163.com/api/search/get/web?s=%E6%99%B4%E5%A4%A9&type=1&limit=3&offset=0" \
  -H "User-Agent: Mozilla/5.0" \
  -H "Referer: https://music.163.com/")

if echo "$response" | grep -q '"code":200'; then
    echo "✓ 搜索成功"
    # 提取歌曲名称（简单示例）
    echo "$response" | grep -o '"name":"[^"]*"' | head -3
else
    echo "✗ 搜索失败"
fi

echo ""
echo "=== 测试完成 ==="
