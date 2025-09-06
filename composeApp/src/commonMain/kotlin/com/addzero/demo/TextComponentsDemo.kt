package com.addzero.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.addzero.annotation.Route
import com.addzero.component.text.*

/**
 * 文本组件演示页面
 */
@Composable
@Route("组件示例", "文本组件", routePath = "/component/text")
fun TextComponentsDemo() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        H1(text = "文本组件演示")

        BodyLarge(text = "展示各种文本组件的样式和用法")

        H2(text = "标题级别")

        H1(text = "H1 标题 - 24sp Bold")
        H2(text = "H2 标题 - 20sp Bold")
        H3(text = "H3 标题 - 18sp SemiBold")
        H4(text = "H4 标题 - 16sp Medium")

        H2(text = "正文级别")

        BodyLarge(text = "BodyLarge - 16sp Normal")
        BodyMedium(text = "BodyMedium - 14sp Normal")
        BodySmall(text = "BodySmall - 12sp Normal")

        H2(text = "辅助文本")

        Caption(text = "Caption - 12sp Normal (浅色)")

        H2(text = "自定义样式")

        Text(
            text = "自定义文本 - 18sp Bold 红色",
            modifier = Modifier,
            color = Color.Red,
            fontSize = 18.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = null
        )

        Text(
            text = "居中文本",
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
