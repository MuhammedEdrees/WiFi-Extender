package org.codeslu.wifiextender.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.ui.theme.Blue500
import org.codeslu.wifiextender.ui.theme.BlueBlack
import org.codeslu.wifiextender.ui.theme.WhitePurple
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme

@Composable
fun ConnectedDeviceItem(
    modifier: Modifier = Modifier,
    deviceName: String,
    deviceIp: String,
    connectionMetrics: String = "",
    isLast: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_device),
                contentDescription = "Device Icon",
                tint = Color.Unspecified,
                modifier = Modifier.size(80.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = deviceIp,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = connectionMetrics,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        if (!isLast) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_UNDEFINED
)
@Composable
private fun ConnectedDeviceItemPreview() {
    WiFiExtenderTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BlueBlack,
            contentColor = WhitePurple
        ) {
            ConnectedDeviceItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                deviceIp = "192.168.1.1",
                deviceName = "Test Device",
                isLast = false
            )
        }
    }
}