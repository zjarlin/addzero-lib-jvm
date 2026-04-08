package site.addzero.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TodoText(
    title: String,
    description: String = "暂未开放",
    modifier: Modifier = Modifier,
    titleStyle: TextStyle? = null,
    descriptionStyle: TextStyle? = null,
    descriptionColor: Color = Color.Unspecified,
) {
    val resolvedTitleStyle = titleStyle ?: MaterialTheme.typography.headlineSmall
    val resolvedDescriptionStyle = descriptionStyle ?: MaterialTheme.typography.bodyLarge
    val resolvedDescriptionColor = descriptionColor.takeOrElse {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = resolvedTitleStyle,
        )
        Text(
            text = description,
            style = resolvedDescriptionStyle,
            color = resolvedDescriptionColor,
        )
    }
}
