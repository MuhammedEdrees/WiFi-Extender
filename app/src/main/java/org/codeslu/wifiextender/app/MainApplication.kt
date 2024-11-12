package org.codeslu.wifiextender.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.wifi.p2p.WifiP2pGroup
import android.os.Build
import org.codeslu.wifiextender.app.hotspot.HotSpot
import org.codeslu.wifiextender.app.hotspot.HotSpotListener
import kotlin.properties.Delegates

class MainApplication: Application() {
    private var listner: HotSpotListener? = null
    var hotspot by Delegates.observable<HotSpot?>(null) { _, _, new ->
        listner?.onNewHotSpot(new)
    }

    var peerList by Delegates.observable<WifiP2pGroup?>(null) { _, _, new ->
        listner?.onNewDeviceList(new)
    }

    fun setHotSpotListener(newListener: HotSpotListener) {
        listner = newListener
    }

    fun removeHotSpotListener() {
        listner = null
    }
}