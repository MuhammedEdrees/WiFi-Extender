package org.codeslu.wifiextender.app.hotspot

import android.net.wifi.p2p.WifiP2pGroup
import org.codeslu.wifiextender.app.hotspot.HotSpot

interface HotSpotListener {

    fun onNewHotSpot(hotSpot: HotSpot?)

    fun onNewDeviceList(new: WifiP2pGroup?)

}