# compose-apple-corner

Apple 风格圆角 `Modifier` 扩展，统一封装树、侧栏、卡片这类容器的 G2 / Apple rounded corner 表达。

- Maven coordinate: `site.addzero:compose-apple-corner`
- Local module path: `lib/compose/compose-apple-corner`

## Usage

```kotlin
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.compose.applecorner.AppleRoundedDefaults
import site.addzero.compose.applecorner.appleRounded

Box(
    modifier = Modifier
        .appleRounded(
            radius = AppleRoundedDefaults.Large,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        )
        .padding(horizontal = 14.dp, vertical = 10.dp),
) {
    Text("Apple rounded container")
}
```

## Notes

- 目前基于 Compose 自带 `Shape` / `Modifier` 组合封装，不额外引入 UI 运行时依赖
- 默认提供 `12 / 14 / 18 / 22 / pill` 这组 Apple rounded 常用半径
- 既支持直接传 `Dp` 半径，也支持传已有 `Shape`
