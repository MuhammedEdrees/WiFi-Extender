package org.codeslu.wifiextender.ui.main

import android.content.res.Configuration
import android.net.wifi.p2p.WifiP2pDevice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.app.MainActivity
import org.codeslu.wifiextender.app.MainApplication
import org.codeslu.wifiextender.app.proxy.server.ProxyServer
import org.codeslu.wifiextender.app.proxy.service.ProxyService
import org.codeslu.wifiextender.ui.main.components.ConnectedDevicesSection
import org.codeslu.wifiextender.ui.main.components.ConnectionInstructionsSection
import org.codeslu.wifiextender.ui.main.components.EditableTextSection
import org.codeslu.wifiextender.ui.main.components.ExpandableSection
import org.codeslu.wifiextender.ui.main.components.HotspotActionsSection
import org.codeslu.wifiextender.ui.main.components.MainTopAppBar
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    onChangeHotspotName: (String) -> Unit,
    onChangeHotspotPassword: (String) -> Unit,
    onConnect: () -> Unit,
    onToggleHotspot: () -> Unit,
    onToggleKeepScreenOn: () -> Unit,
    onToggleWPS: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MainScreenContent(
        modifier = modifier,
        isLoading = state.isLoading,
        isConnected = state.isConnected,
        isHotspotStarted = state.isHotspotStarted,
        isWPSOn = state.isWPSOn,
        isKeepScreenOn = state.isKeepScreenOn,
        connectedDevices = state.connectedDevices,
        hotspotName = state.hotspotName,
        hotspotPassword = state.hotspotPassword,
        onAction = { action ->
            when (action) {
                is MainUiAction.OnChangeHotspotName -> onChangeHotspotName(action.newName)
                is MainUiAction.OnChangeHotspotPassword -> onChangeHotspotPassword(action.newPassword)
                MainUiAction.OnConnect -> onConnect()
                MainUiAction.OnSwitchKeepScreenOn -> onToggleKeepScreenOn()
                MainUiAction.OnSwitchWPS -> onToggleWPS()
                MainUiAction.OnToggleHotspot -> onToggleHotspot()
                else -> Unit
            }
        }
    )

}


@Composable
private fun MainScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isConnected: Boolean = false,
    isHotspotStarted: Boolean = false,
    isWPSOn: Boolean = false,
    isKeepScreenOn: Boolean = false,
    connectedDevices: Collection<WifiP2pDevice> = emptyList(),
    hotspotName: String = "",
    hotspotPassword: String = "",
    onAction: (MainUiAction) -> Unit = { }
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            MainTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                isConnected = isConnected,
                onToggleConnect = {
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
        ) {
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            EditableTextSection(
                modifier = Modifier.padding(top = 24.dp),
                isActive = isHotspotStarted,
                title = stringResource(R.string.name),
                value = hotspotName,
                hasPrefix = true,
                prefix = ProxyService.NAME_PREFIX,
                onUpdateValue = { newValue ->
                    onAction(MainUiAction.OnChangeHotspotName(newValue))
                }
            )
            EditableTextSection(
                modifier = Modifier.padding(top = 24.dp),
                isActive = isHotspotStarted,
                title = stringResource(R.string.password),
                value = hotspotPassword,
                onUpdateValue = { newValue ->
                    onAction(MainUiAction.OnChangeHotspotPassword(newValue))
                }
            )
            HotspotActionsSection(
                modifier = Modifier.padding(top = 24.dp),
                isHotspotStarted = isHotspotStarted,
                onToggleHotspot = {
                    onAction(MainUiAction.OnToggleHotspot)
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
            Crossfade(
                targetState = isHotspotStarted,
                label = "connected devices"
            ) { state ->
                if (state) {
                    Column(modifier = Modifier.fillMaxWidth()){
                        ExpandableSection(
                            modifier = Modifier.padding(top = 24.dp),
                            title = stringResource(R.string.connection_instructions)
                        ) {
                            ConnectionInstructionsSection()
                        }
                        ExpandableSection(
                            modifier = Modifier.padding(top = 24.dp),
                            title = stringResource(R.string.connected_devices)
                        ) {
                            ConnectedDevicesSection(
                                devices = connectedDevices
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(
    showSystemUi = true, showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_UNDEFINED
)
@Composable
private fun MainScreenContentPreview() {
    WiFiExtenderTheme {
        MainScreenContent(
            isHotspotStarted = true,
        )
    }
}