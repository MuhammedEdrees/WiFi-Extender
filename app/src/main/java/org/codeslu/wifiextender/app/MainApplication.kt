package org.codeslu.wifiextender.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.wifi.p2p.WifiP2pGroup
import android.os.Build
import androidx.annotation.RequiresApi
import org.codeslu.wifiextender.app.di.appModule
import org.codeslu.wifiextender.app.hotspot.HotSpot
import org.codeslu.wifiextender.app.hotspot.HotSpotListener
import org.codeslu.wifiextender.app.proxy.service.ProxyService
import org.codeslu.wifiextender.app.vpn.VpnListener
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import kotlin.properties.Delegates

class MainApplication : Application() {
    private var hotspotListener: HotSpotListener? = null
    private var vpnListener: VpnListener? = null
    var isVpnStarted by Delegates.observable(false) { _, _, new ->
        vpnListener?.onNewVpnState(new)
    }
    var hotspot by Delegates.observable<HotSpot?>(null) { _, _, new ->
        hotspotListener?.onNewHotSpot(new)
    }

    var peerList by Delegates.observable<WifiP2pGroup?>(null) { _, _, new ->
        hotspotListener?.onNewDeviceList(new)
    }

    fun setHotSpotListener(newListener: HotSpotListener) {
        hotspotListener = newListener
    }

    fun removeHotSpotListener() {
        hotspotListener = null
    }

    fun setVpnListener(newListener: VpnListener) {
        vpnListener = newListener
    }

    fun removeVpnListener() {
        vpnListener = null
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val chan = NotificationChannel(
            ProxyService.CHANNEL_ID,
            ProxyService.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }
}