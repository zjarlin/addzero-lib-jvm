package site.addzero.abs

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun GeneratedComposable() {
    Box(
        modifier = Modifier.offset(-12.dp, -12.dp).size(1442.dp, 2028.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFFFFFFF))
            .padding(20.dp),
    ) {
        Box(
            modifier = Modifier.offset(20.dp, 20.dp).size(1402.dp, 1988.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFFFFF))
                .padding(20.dp),
        ) {
            Button(onClick = { }, modifier = Modifier.offset(384.dp, 250.dp).size(180.dp, 56.dp)) {
                Text("Button")
            }
            Text(text = "Text", modifier = Modifier.offset(184.dp, 319.dp).size(160.dp, 48.dp))
        }
    }
}
