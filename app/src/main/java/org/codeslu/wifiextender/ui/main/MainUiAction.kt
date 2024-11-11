package org.codeslu.wifiextender.ui.main

sealed interface MainUiAction {
    data object OnConnectClicked: MainUiAction
    data object OnStartHotspotClicked: MainUiAction
}