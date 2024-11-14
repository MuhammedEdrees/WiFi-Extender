package org.codeslu.wifiextender.app.vpn

interface VpnListener {
    fun onNewVpnState(isStarted: Boolean?)
}