package site.addzero.abs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun GeneratedComposable() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7))
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier.offset(28.dp, 28.dp).size(820.dp, 560.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFFFFF))
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.offset(201.dp, 121.dp).size(314.dp, 211.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFFFFFF))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Image(painter = painterResource("placeholder.png"), contentDescription = null, modifier = Modifier.size(160.dp, 120.dp))
                Text(text = "Text", modifier = Modifier.size(160.dp, 48.dp))
            }
        }
        Spacer(modifier = Modifier.offset(126.dp, 638.dp).size(226.dp, 63.dp))
        Text(text = "Text", modifier = Modifier.offset(122.dp, 793.dp).size(160.dp, 48.dp))
    }
}
