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
        modifier = Modifier.offset(8.dp, 8.dp).size(1562.dp, 2148.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFFFFFFF))
            .padding(20.dp),
    ) {
        Box(
            modifier = Modifier.offset(20.dp, 20.dp).size(1522.dp, 2108.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFFFFF))
                .padding(20.dp),
        ) {
            Text(text = "Text", modifier = Modifier.offset(268.dp, 58.dp))
            Text(text = "Text", modifier = Modifier.offset(674.dp, 162.dp))
            Spacer(modifier = Modifier.offset(72.dp, 171.dp).size(300.dp, 70.dp))
            Row(
                modifier = Modifier.offset(434.dp, 248.dp).size(293.dp, 252.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFFFFFF))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            }
            Column(
                modifier = Modifier.offset(730.dp, 478.dp).size(220.dp, 160.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFFFFFF))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(text = "Text", modifier = Modifier)
                Box(modifier = Modifier.size(96.dp, 96.dp).clip(RoundedCornerShape(22.dp)).background(Color(0xFFE9EEF6)))
            }
            Row(
                modifier = Modifier.offset(90.dp, 552.dp).size(411.dp, 333.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFFFFFF))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(onClick = { }, modifier = Modifier) {
                    Text("Button")
                }
                IconPlaceholder(modifier = Modifier)
            }
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
