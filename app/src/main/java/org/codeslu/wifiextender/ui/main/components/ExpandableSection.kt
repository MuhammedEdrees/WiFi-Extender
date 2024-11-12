package org.codeslu.wifiextender.ui.main.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme

@Composable
fun ExpandableSection(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Column (
        modifier = modifier.fillMaxWidth().animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rotationAngle by animateFloatAsState(
                targetValue = if (!isExpanded) 180f else 0f,
                animationSpec = tween(durationMillis = 500),
                label = "arrow rotation"
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer(rotationX = rotationAngle)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W500
            )
        }
        if (isExpanded){
            content()
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ExpandableSectionPreview() {
    WiFiExtenderTheme {
        ExpandableSection(
            modifier = Modifier.padding(16.dp),
            title = "Expandable Section"
        ) {
            Text(
                text = "This is an expandable section",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400
            )
        }
    }

}