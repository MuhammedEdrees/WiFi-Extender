package org.codeslu.wifiextender.ui.main.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
fun MainBottomBar(
    modifier: Modifier = Modifier,
    isHotspotStarted: Boolean,
    onHotSpotButtonClicked: () -> Unit,
    onSettingsClicked: () -> Unit = {},
    isWPSOn: Boolean,
    onWPSSwitched: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            thickness = 1.dp, color = MaterialTheme.colorScheme.outline
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Crossfade(
                modifier = Modifier.align(Alignment.CenterStart),
                targetState = isHotspotStarted,
                label = "WPS Button"
            ) { state ->
                when {
                    state -> {
                        WPSSwitch(
                            isWPSOn = isWPSOn, onWPSSwitched = onWPSSwitched
                        )
                    }

                    else -> {}
                }
            }
            OutlinedIconButton(
                shape = CircleShape, colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ), onClick = { onSettingsClicked() }, modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = null
                )
            }


        }
    }

}

@Composable
private fun WPSSwitch(
    modifier: Modifier = Modifier, isWPSOn: Boolean, onWPSSwitched: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Switch(checked = isWPSOn, onCheckedChange = {
            onWPSSwitched()
        })
        Text(
            text = stringResource(R.string.wps),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.W400,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun MainBottomBarPreview() {
    WiFiExtenderTheme {

        MainBottomBar(
            modifier = Modifier.fillMaxWidth(),
            isHotspotStarted = false,
            onHotSpotButtonClicked = {},
            isWPSOn = true,
            onWPSSwitched = {}
        )
    }
}