package org.codeslu.wifiextender.ui.main

import android.net.wifi.p2p.WifiP2pDevice

sealed interface MainUiAction {
    data object OnConnect: MainUiAction
    data class OnUpdateConnectedFlag(val value: Boolean): MainUiAction
    data class OnSetLoading(val value: Boolean): MainUiAction
    data class OnSetConnectedDevices(val connectedDevices: List<WifiP2pDevice>): MainUiAction
    data class OnSetHotspotName(val newName: String): MainUiAction
    data class OnSetHotspotPassword(val newPassword: String): MainUiAction
    data object OnToggleHotspot: MainUiAction
    data class OnUpdateHotspotFlag(val value: Boolean): MainUiAction
    data object OnSwitchWPS: MainUiAction
    data class OnUpdateWPSFlag(val value: Boolean): MainUiAction
    data object OnSwitchKeepScreenOn: MainUiAction
    data class OnUpdateKeepScreenOnFlag(val value: Boolean): MainUiAction
    data object OnConfigureHotspots: MainUiAction
    data class OnChangeHotspotName(val newName: String): MainUiAction
    data class OnChangeHotspotPassword(val newPassword: String): MainUiAction
}