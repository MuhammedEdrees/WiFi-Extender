package org.codeslu.wifiextender.app.proxy.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.app.MainActivity
import org.codeslu.wifiextender.app.MainApplication
import org.codeslu.wifiextender.app.WiFiDirectBroadcastReceiver
import org.codeslu.wifiextender.app.hotspot.HotSpot
import org.codeslu.wifiextender.app.proxy.server.ProxyServer

class ProxyService : Service() {
    private var proxyServer: ProxyServer? = null

    companion object {
        const val TAG = "ProxyService"
        const val NAME_PREFIX = "DIRECT-WE-"
        const val CHANNEL_ID = "WiFiExtenderServiceChannel"
        const val CHANNEL_NAME = "WiFiExtenderService"
        const val PREF_NAME = "pref_wifi_extender"
        const val KEY_NAME = "key_name"
        const val DEFAULT_NAME = "Hotspot"
        const val KEY_PASSWORD = "key_password"
        const val DEFAULT_PASSWORD = "87654321"

    }

    private var enabled: Boolean = false
    private var name: String? = null
    private var password: String? = null
    private lateinit var manager: WifiP2pManager
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val receiver =
        WiFiDirectBroadcastReceiver(object : WiFiDirectBroadcastReceiver.Listener {

            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "received intent: ${intent?.action}")
                when (intent?.action) {
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        Log.d(TAG, "insode P2P Peers changed action")
                        val channel = manager.initialize(this@ProxyService, mainLooper, null)
                        manager.requestGroupInfo(channel) {
                            (application as MainApplication).peerList = it
                            Log.d(TAG, "new Peer List size = ${it.clientList.size}")
                            updateNotification()
                        }
                    }

                    else -> Unit
                }
            }
        }
        )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        registerReceiver(receiver, intentFilter)
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            Actions.START.toString() -> {
                start()
            }

            Actions.STOP.toString() -> {
                stopHotspot()
            }
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun start() {
        startForeground(1, buildNotification())
        startHotSpot()
    }

    @SuppressLint("ForegroundServiceType")
    private fun updateNotification() {
        startForeground(1, buildNotification())
    }

    private fun buildNotification(): Notification {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val largeIconBitmap = BitmapFactory.decodeResource(this@ProxyService.resources, R.drawable.ic_logo)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi Extender running")
            .setContentText("Connected Devices: ${(application as? MainApplication)?.peerList?.clientList?.size ?: 0}")
            .setSmallIcon(R.drawable.ic_logo)
            .setLargeIcon(largeIconBitmap)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startHotSpot() {
        val channel = manager.initialize(this, mainLooper, null)
        val actionListener = object : WifiP2pManager.ActionListener {
            @SuppressLint("MissingPermission")
            override fun onSuccess() {
                Log.d(TAG, "createGroup: onSuccess")
                Thread.sleep(500)
                manager.requestGroupInfo(channel) { group ->
                    name = group.networkName
                    password = group.passphrase
                    Log.d(
                        TAG,
                        "wake up network:\nnetworkName: $name\npassphrase: $password"
                    )
                    manager.requestConnectionInfo(channel) { p0 ->
                        val ip = p0?.groupOwnerAddress?.hostAddress
                        if (ip != null) {
                            (application as? MainApplication)?.hotspot =
                                HotSpot(group.networkName, group.passphrase)
                            Log.w(TAG, "My IP is $ip")
                            runProxy(address = ip)
                        } else {
                            Log.e(TAG, "Group Owner Address is null. Cannot start proxy.")
                        }
                    }
                }

            }

            override fun onFailure(reason: Int) {
                Log.e(
                    TAG, "createGroup onFailure: ${
                        when (reason) {
                            WifiP2pManager.ERROR -> "ERROR"
                            WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                            WifiP2pManager.BUSY -> "BUSY"
                            else -> "UNKNOWN"
                        }
                    }"
                )
                stopHotspot()
            }

        }
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val hotspotName = prefs.getString(KEY_NAME, DEFAULT_NAME)
        val hotspotPassword = prefs.getString(KEY_PASSWORD, DEFAULT_PASSWORD)
        Log.d(TAG, "prefs: hotspotName: $hotspotName, hotspotPassword: $hotspotPassword")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && hotspotName != null && hotspotPassword != null) {
            manager.createGroup(
                channel,
                WifiP2pConfig.Builder().setNetworkName(NAME_PREFIX + hotspotName)
                    .setPassphrase(hotspotPassword)
                    .build(),
                actionListener
            )
        } else {
            manager.createGroup(channel, actionListener)
        }
    }


    private fun stopHotspot() {
        Log.d(TAG, "stopHotspot")
        val channel = manager.initialize(this, mainLooper, null)
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "removeGroup onSuccess")
            }

            override fun onFailure(reason: Int) {
                Log.e(
                    TAG, "removeGroup onFailure: ${
                        when (reason) {
                            WifiP2pManager.ERROR -> "ERROR"
                            WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                            WifiP2pManager.BUSY -> "BUSY"
                            else -> "UNKNOWN"
                        }
                    }"
                )

            }
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            channel.close()
        }
        enabled = false
        (application as? MainApplication)?.hotspot = null
        (application as? MainApplication)?.peerList = null
        proxyServer?.stopProxy()
        stopSelf()
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
        enabled = true
    }

    enum class Actions {
        START,
        STOP
    }
}