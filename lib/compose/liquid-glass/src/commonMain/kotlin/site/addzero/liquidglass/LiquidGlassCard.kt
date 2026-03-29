package site.addzero.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    spec: LiquidGlassSpec = LiquidGlassDefaults.card,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .liquidGlassSurface(spec),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(spec.shape),
        ) {
            Box(modifier = cardBaseTone())
            Box(modifier = cardTopCornerGlow())
            Box(modifier = cardTopCornerSpark())
            Box(modifier = cardBottomCornerGlow())
            Box(modifier = cardBottomCornerSpark())
            Box(modifier = cardTopCornerCaustic())
            Box(modifier = cardBottomCornerCaustic())
        }

        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            content = content,
        )
    }
}

@Composable
fun LiquidGlassCardHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                color = LiquidGlassDefaults.textPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = LiquidGlassDefaults.textSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (badge != null) {
            Text(
                text = badge,
                color = LiquidGlassDefaults.textPrimary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .liquidGlassSurface(LiquidGlassDefaults.sidebarItemSelected)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

/** 底色漂洗层：给玻璃内部一点轻微冷暖偏色，避免整块发灰。 */
private fun BoxScope.cardBaseTone(): Modifier =
    Modifier.matchParentSize().background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.006f),
                Color.Transparent,
                LiquidGlassDefaults.accent.copy(alpha = 0.008f),
            ),
        ),
    )

/** 左上角主高光：模拟玻璃内部受光，不直接描边。 */
private fun BoxScope.cardTopCornerGlow(): Modifier =
    Modifier.align(Alignment.TopStart)
        .offset(x = (-6).dp, y = (-8).dp)
        .size(126.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color(0xFFCFEFFF).copy(alpha = 0.04f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(28.dp)

/** 左上角细小火花：补一点更集中的高频亮度。 */
private fun BoxScope.cardTopCornerSpark(): Modifier =
    Modifier.align(Alignment.TopStart)
        .offset(x = 24.dp, y = 20.dp)
        .size(62.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.04f),
                    Color(0xFFDDF5FF).copy(alpha = 0.02f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(16.dp)

/** 右下角主折射色散：只在内部发光，避免外框发白。 */
private fun BoxScope.cardBottomCornerGlow(): Modifier =
    Modifier.align(Alignment.BottomEnd)
        .offset(x = 8.dp, y = 8.dp)
        .size(144.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidGlassDefaults.accentStrong.copy(alpha = 0.08f),
                    Color(0xFFDAF7FF).copy(alpha = 0.03f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(32.dp)

/** 右下角补光：让材质边缘更像水体而不是硬描边。 */
private fun BoxScope.cardBottomCornerSpark(): Modifier =
    Modifier.align(Alignment.BottomEnd)
        .offset(x = (-8).dp, y = (-8).dp)
        .size(72.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.03f),
                    LiquidGlassDefaults.accent.copy(alpha = 0.016f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(18.dp)

/** 左上角焦散团：用弧形团状高光替代线段，避免标题区出现矩形笔触。 */
private fun BoxScope.cardTopCornerCaustic(): Modifier =
    Modifier.align(Alignment.TopStart)
        .offset(x = 18.dp, y = 14.dp)
        .size(108.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.04f),
                    Color(0xFFDDF7FF).copy(alpha = 0.018f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(24.dp)

/** 右下角焦散团：去掉长条扫光，改成更像折射残影的软椭圆光斑。 */
private fun BoxScope.cardBottomCornerCaustic(): Modifier =
    Modifier.align(Alignment.BottomEnd)
        .offset(x = (-20).dp, y = (-18).dp)
        .size(width = 120.dp, height = 52.dp)
        .rotate(-16f)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.02f),
                    LiquidGlassDefaults.accentStrong.copy(alpha = 0.022f),
                    Color.Transparent,
                ),
            ),
            shape = RoundedCornerShape(999.dp),
        )
        .blur(18.dp)
