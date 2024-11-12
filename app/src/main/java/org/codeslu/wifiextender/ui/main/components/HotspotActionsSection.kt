package org.codeslu.wifiextender.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme

@Composable
fun HotspotActionsSection(
    modifier: Modifier = Modifier,
    isHotspotStarted: Boolean,
    onHotspotButtonClicked: () -> Unit,
    onConfigureHotspotsClicked: () -> Unit = {},
    isWPSOn: Boolean,
    onWPSSwitched: () -> Unit = {},
    isKeepScreenOn: Boolean,
    onKeepScreenOnSwitched: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                modifier = Modifier
                    .height(40.dp),
                onClick = { onHotspotButtonClicked() }
            ) {
                Text(
                    text = stringResource(id = if (isHotspotStarted) R.string.stop_wifi_hotspot else R.string.start_wifi_hotspot),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.W500
                )
            }
            LabeledSwitch(
                label = stringResource(id = R.string.wps),
                isChecked = isWPSOn,
                onSwitch = { onWPSSwitched() }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                modifier = Modifier
                    .height(40.dp),
                onClick = { onConfigureHotspotsClicked() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(id = R.string.configure_hotspots),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            LabeledSwitch(
                label = stringResource(id = R.string.keep_screen_on),
                isChecked = isKeepScreenOn,
                onSwitch = { onKeepScreenOnSwitched() }
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HotspotActionsSectionPreview() {
    WiFiExtenderTheme {
        HotspotActionsSection(
            modifier = Modifier.padding(horizontal = 16.dp),
            isHotspotStarted = true,
            onHotspotButtonClicked = {},
            isWPSOn = true,
            onWPSSwitched = {},
            isKeepScreenOn = true,
            onKeepScreenOnSwitched = {}
        )
    }
}