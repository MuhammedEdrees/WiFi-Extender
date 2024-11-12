package org.codeslu.wifiextender.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.ui.main.components.ConnectionInstructionsSection
import org.codeslu.wifiextender.ui.main.components.EditableTextSection
import org.codeslu.wifiextender.ui.main.components.ExpandableSection
import org.codeslu.wifiextender.ui.main.components.HotspotActionsSection
import org.codeslu.wifiextender.ui.main.components.MainBottomBar
import org.codeslu.wifiextender.ui.main.components.MainTopAppBar
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme

@Composable
fun MainScreen(modifier: Modifier = Modifier) {

}


@Composable
private fun MainScreenContent(
    modifier: Modifier = Modifier,
    isConnected: Boolean = false,
    isHotspotStarted: Boolean = false,
    isWPSOn: Boolean = false,
    isKeepScreenOn: Boolean = false,
    hotspotName: String = "",
    hotspotPassword: String = "",
    onAction: (MainUiAction) -> Unit = { }
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                onConnect = {
                    onAction(MainUiAction.OnConnect)
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            EditableTextSection(
                isActive = isHotspotStarted,
                title = stringResource(R.string.name),
                value = hotspotName,
                onUpdateValue = { newValue ->
                    onAction(MainUiAction.OnChangeHotspotName(newValue))
                }
            )
            EditableTextSection(
                isActive = isHotspotStarted,
                title = stringResource(R.string.password),
                value = hotspotPassword,
                onUpdateValue = { newValue ->
                    onAction(MainUiAction.OnChangeHotspotPassword(newValue))
                }
            )
            HotspotActionsSection(
                isHotspotStarted = isHotspotStarted,
                onHotspotButtonClicked = {
                    onAction(MainUiAction.OnSwitchHotspot)
                },
                onConfigureHotspotsClicked = {
                    onAction(MainUiAction.OnConfigureHotspots)
                },
                isWPSOn = isWPSOn,
                onWPSSwitched = {
                    onAction(MainUiAction.OnSwitchWPS)
                },
                isKeepScreenOn = isKeepScreenOn,
                onKeepScreenOnSwitched = {
                    onAction(MainUiAction.OnSwitchKeepScreenOn)
                }
            )
            ExpandableSection(title = stringResource(R.string.connection_instructions)) {
                ConnectionInstructionsSection()
            }
            ExpandableSection(title = stringResource(R.string.connected_devices)) {
                //Connected Devices
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun MainScreenContentPreview() {
    WiFiExtenderTheme {
        MainScreenContent(
            isHotspotStarted = true,
        )
    }
}