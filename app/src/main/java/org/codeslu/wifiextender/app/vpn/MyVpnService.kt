package org.codeslu.wifiextender.app.vpn

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.VpnService
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.app.MainActivity
import org.codeslu.wifiextender.app.MainApplication
import org.codeslu.wifiextender.app.WiFiDirectBroadcastReceiver
import org.codeslu.wifiextender.app.proxy.service.ProxyService
import org.codeslu.wifiextender.app.proxy.service.ProxyService.Companion.CHANNEL_ID
import java.net.DatagramSocket
import java.net.InetSocketAddress

class MyVpnService : VpnService() {
    companion object {
        const val TAG = "MyVpnService"
        const val START_ACTION: String = "org.codeslu.wifiextender.app.vpn.START"
        const val STOP_ACTION: String = "org.codeslu.wifiextender.app.vpn.STOP"
        const val NAME: String = "wifi-extender-connection"
    }

    private var isRunning: Boolean = false
    private var tunnel: DatagramSocket? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private var serverIp: String = ""
    private var serverPort: Int = 0
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

                    }

                    else -> Unit
                }
            }
        }
        )

    override fun onCreate() {
        Log.d(TAG, "VPN service created")
        super.onCreate()
        registerReceiver(receiver, intentFilter)
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    override fun onDestroy() {
        stopVpn()
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VPN service onStartCommand")
        when (intent?.action) {
            START_ACTION -> {
                start()
            }

            STOP_ACTION -> {
                stopVpn()
            }
        }

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun start() {
        val channel = manager.initialize(this, mainLooper, null)
        val request = WifiP2pDnsSdServiceRequest.newInstance(
            ProxyService.BONJOUR_SERVICE_NAME,
            ProxyService.BONJOUR_SERVICE_TYPE
        )
        val addServiceListener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Service request added")
            }

            override fun onFailure(p0: Int) {
                Log.e(TAG, "Failed to add service request: $p0")
                stopSelf()
            }

        }
        val dnsSdResponseListener =
            WifiP2pManager.DnsSdServiceResponseListener { p0, p1, p2 ->
                Log.d(
                    TAG,
                    "DnsSd service available"
                )
            }
        val dnsSdTxtRecordListener =
            WifiP2pManager.DnsSdTxtRecordListener { p0, p1, p2 ->
                Log.d(TAG, "DNS record available")
                serverIp = p1?.get(ProxyService.PROXY_IP_KEY) ?: ""
                serverPort = p1?.get(ProxyService.PROXY_PORT_KEY)?.toInt() ?: 0
                if (serverIp.isNotBlank() && serverPort > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            startVpn()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to start VPN", e)
                            stopSelf()
                        }
                    }
                } else {
                    Log.d(TAG, "Server IP or port not available")
                    stopSelf()
                }
            }
        manager.addServiceRequest(channel, request, addServiceListener)
        manager.setDnsSdResponseListeners(channel, dnsSdResponseListener, dnsSdTxtRecordListener)
        manager.discoverServices(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Service discovery started")
            }

            override fun onFailure(p0: Int) {
                Log.d(TAG, "Service discovery failed")
                stopSelf()
            }

        }
        )
    }

    @SuppressLint("ForegroundServiceType")
    private fun startVpn() {
        if (isRunning || serverIp.isBlank() || serverPort <= 0) {
            Log.e(TAG, "Invalid server IP or port or vpn is already running")
            stopSelf()
            return
        }
        Log.d(TAG, "VPN starting")
        tunnel = DatagramSocket()
        protect(tunnel)
        tunnel?.connect(InetSocketAddress(serverIp, serverPort))
        val builder = Builder()
            .setSession(NAME)
            .addAddress(serverIp, 24)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
        vpnInterface = builder.establish()
        Log.d(TAG, "VPN established ${vpnInterface?.fd}")
        vpnInterface?.let {
            val notification = createNotification()
            startForeground(1, notification)
        }
        isRunning = true
        application?.let {
            (it as MainApplication).isVpnStarted = true
        }
    }


    private fun createNotification(): Notification {
        Log.d(TAG, "Creating notification")
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val largeIconBitmap =
            BitmapFactory.decodeResource(this@MyVpnService.resources, R.drawable.ic_logo)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi Extender running")
            .setContentText("Connected")
            .setSmallIcon(R.drawable.ic_logo)
            .setLargeIcon(largeIconBitmap)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .build()
    }

    private fun stopVpn() {
        if (!isRunning) {
            return
        }
        val channel = manager.initialize(this, mainLooper, null)
        manager.clearServiceRequests(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Service requests cleared")
            }

            override fun onFailure(p0: Int) {
                Log.e(TAG, "Failed to clear service requests: $p0")
            }
        })
        isRunning = false
        vpnInterface?.close()
        vpnInterface = null
        tunnel?.close()
        application?.let {
            (it as MainApplication).isVpnStarted = false
        }
        stopSelf()
    }


    override fun onRevoke() {
        Log.d(TAG, "VPN revoked")
        stopVpn()
        super.onRevoke()
    }
}