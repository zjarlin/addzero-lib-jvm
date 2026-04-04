package site.addzero.abs

import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GeneratedComposable() {
    Box(
        modifier = Modifier.offset(-36.dp, -50.dp).size(884.dp, 638.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFFFFFFF))
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.offset(131.dp, 261.dp).size(388.dp, 147.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFFFFF))
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconPlaceholder(modifier = Modifier)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Text", modifier = Modifier)
            Spacer(modifier = Modifier.width(16.dp))
            Spacer(modifier = Modifier.size(100.dp, 24.dp))
        }
    }
}

@Composable
private fun IconPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(40.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFDCE6F5)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "◎")
    }
}
