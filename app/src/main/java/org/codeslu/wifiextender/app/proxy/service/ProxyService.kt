package org.codeslu.wifiextender.app.proxy.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.app.proxy.server.ProxyServer
import org.codeslu.wifiextender.ui.MainActivity

class ProxyService : Service() {
    private var proxyServer: ProxyServer? = null

    companion object {
        const val TAG = "ProxyService"

        const val NAME_PREFIX = "DIRECT-We-"

        const val CHANNEL_ID = "WiFiExtenderServiceChannel"
        const val CHANNEL_NAME = "WiFiExtenderService"

        const val STOP_HOTSPOT = "stop_hotspot"

        const val PREF_NAME = "pref_wifi_extender"

        const val KEY_ENABLED = "key_enabled"
        const val KEY_NAME = "key_name"
        const val KEY_PASSWORD = "key_password"

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                start()
            }

            Actions.STOP.toString() -> {
                stopHotspot()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("ForegroundServiceType")
    private fun start() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, createNotificationChannel())
            .setContentTitle("Wi-Fi Extender running")
            .setContentText("Tethered 0:46 MB")
            .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        val notification = builder.build()
        startForeground(1, notification)
        startHotSpot()
    }

    private fun startHotSpot() {
//        TODO("Not yet implemented")
        runProxy()
    }

    private fun stopHotspot() {
//        TODO("Not yet implemented")
        proxyServer?.stopProxy()
        stopSelf()
    }

    private fun createNotificationChannel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        } else {
            CHANNEL_ID
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun runProxy(
        port: Int = 8181,
        address: String = "0.0.0.0"
    ) {
        proxyServer = if (proxyServer == null) {
            ProxyServer(port, address)
        } else {
            proxyServer
        }
        proxyServer?.startProxy()
    }

    enum class Actions {
        START,
        STOP
    }
}