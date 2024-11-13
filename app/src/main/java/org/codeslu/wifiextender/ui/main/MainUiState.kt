package org.codeslu.wifiextender.ui.main

import android.net.wifi.p2p.WifiP2pDevice
import org.codeslu.wifiextender.app.proxy.service.ProxyService

data class MainUiState(
    val connectedDevices: List<WifiP2pDevice> = emptyList(),
    val hotspotName: String = ProxyService.DEFAULT_NAME,
    val hotspotPassword: String = ProxyService.DEFAULT_PASSWORD,
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val isHotspotStarted: Boolean = false,
    val isWPSOn: Boolean = false,
    val isKeepScreenOn: Boolean = false,
)
