package org.codeslu.wifiextender.ui.main

sealed interface MainUiAction {
    data object OnConnect: MainUiAction
    data object OnSwitchHotspot: MainUiAction
    data object OnSwitchWPS: MainUiAction
    data object OnSwitchKeepScreenOn: MainUiAction
    data object OnConfigureHotspots: MainUiAction
    data class OnChangeHotspotName(val name: String): MainUiAction
    data class OnChangeHotspotPassword(val password: String): MainUiAction
}