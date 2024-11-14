package org.codeslu.wifiextender.app.vpn

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.NetworkInfo
import android.net.VpnService
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
        const val VPN_DNS_SERVER: String = "8.8.8.8"
        const val VPN_ROUTE: String = "0.0.0.0"
    }

    private var isRunning: Boolean = false
    private var isConnected: Boolean = false
    private var isDiscoveringServices = false
    private var config: WifiP2pConfig? = null
    private var tunnel: DatagramSocket? = null
    private var vpnInterface: ParcelFileDescriptor? = null
    private var serverIp: String = ""
    private var serverPort: Int = 0
    private lateinit var manager: WifiP2pManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val wiFiDirectBroadcastReceiver =
        WiFiDirectBroadcastReceiver(object : WiFiDirectBroadcastReceiver.Listener {

            @Suppress("DEPRECATION")
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "received intent: ${intent?.action}")
                when (intent?.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                            Log.d(TAG, "Wi-Fi Direct is disabled")
                            showEnableWifiDirectDialog()
                        } else if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                            Log.d(TAG, "Wi-Fi Direct is enabled")
                        }
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                        val wifiP2pInfo = intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                        if (networkInfo != null && wifiP2pInfo != null) {
                            if (networkInfo.isConnected) {
                                coroutineScope.launch {
                                    startVpn()
                                }
                                Log.d(TAG, "Group formed with info: $wifiP2pInfo")
                            } else {
                                Log.d(TAG, "Group removed or device disconnected")
                                stopSelf()
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
        )

    fun showEnableWifiDirectDialog() {
        AlertDialog.Builder(applicationContext)
            .setTitle(getString(R.string.enable_wi_fi_direct))
            .setMessage(getString(R.string.wi_fi_direct_is_currently_disabled_please_enable_it_in_settings_to_use_this_feature))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                startSettingsIntent()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
        stopSelf()
    }

    override fun onCreate() {
        Log.d(TAG, "VPN service created")
        super.onCreate()
        checkIfWifiEnabled()
        registerReceiver(wiFiDirectBroadcastReceiver, intentFilter)
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private fun checkIfWifiEnabled() {
        val wifiState =
            (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).wifiState
        if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
            Toast.makeText(
                applicationContext,
                getString(R.string.wi_fi_is_not_enabled_please_enable_wi_fi), Toast.LENGTH_SHORT
            ).show()
            startSettingsIntent()
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopVpn()
        unregisterReceiver(wiFiDirectBroadcastReceiver)
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VPN service onStartCommand")
        when (intent?.action) {
            START_ACTION -> {
                startServiceDiscovery()
            }

            STOP_ACTION -> {
                stopVpn()
            }
        }

        return START_STICKY
    }

    private fun startSettingsIntent() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun startServiceDiscovery() {
        if (isDiscoveringServices) return
        isDiscoveringServices = true
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
            WifiP2pManager.DnsSdTxtRecordListener { _, txtRecord, discoveredDevice ->
                Log.d(TAG, "DNS record available")
                if (isConnected) {
                    return@DnsSdTxtRecordListener
                }
                if (serverIp == txtRecord?.get(ProxyService.PROXY_IP_KEY) && serverPort == txtRecord[ProxyService.PROXY_PORT_KEY]?.toInt()) {
                    return@DnsSdTxtRecordListener
                }
                serverIp = txtRecord?.get(ProxyService.PROXY_IP_KEY) ?: ""
                serverPort = txtRecord?.get(ProxyService.PROXY_PORT_KEY)?.toInt() ?: 0
                if (serverIp.isNotBlank() && serverPort > 0) {
                    config = WifiP2pConfig().apply {
                        deviceAddress = discoveredDevice.deviceAddress
                        wps.setup = WpsInfo.PBC
                    }
                    connectToClient()
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

            override fun onFailure(reason: Int) {
                Log.d(
                    TAG, "Service discovery failed: ${
                        when (reason) {
                            WifiP2pManager.ERROR -> "ERROR"
                            WifiP2pManager.P2P_UNSUPPORTED -> "P2P_UNSUPPORTED"
                            WifiP2pManager.BUSY -> "BUSY"
                            else -> "UNKNOWN"
                        }
                    }"
                )
                stopSelf()
            }
        }
        )
    }

    @SuppressLint("MissingPermission")
    private fun connectToClient() {
        val channel = manager.initialize(this, mainLooper, null)
        coroutineScope.launch {
            startSettingsIntent()
            manager.connect(
                channel,
                config,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(
                            TAG,
                            "Connected to the device offering the service"
                        )
                        isConnected = true
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "Failed to connect to the device: $reason")
                        stopSelf()
                    }
                }
            )
        }
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
            .addRoute(VPN_ROUTE, 0)
            .addDnsServer(VPN_DNS_SERVER)
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