package org.codeslu.wifiextender.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pGroup
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.app.hotspot.HotSpot
import org.codeslu.wifiextender.app.hotspot.HotSpotListener
import org.codeslu.wifiextender.app.proxy.service.ProxyService
import org.codeslu.wifiextender.app.vpn.MyVpnService
import org.codeslu.wifiextender.app.vpn.VpnListener
import org.codeslu.wifiextender.ui.main.MainScreen
import org.codeslu.wifiextender.ui.main.MainUiAction
import org.codeslu.wifiextender.ui.main.MainViewModel
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity(), HotSpotListener, VpnListener {
    private var restartHotSpotAfterStop: Boolean = false
    private lateinit var viewModel: MainViewModel

    companion object {
        const val TAG = "MainActivity"
        const val VPN_REQUEST_CODE = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
                0
            )
        }
        viewModel = getViewModel()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = resources.getColor(R.color.background_dark),
            )
        )
        setContent {
            WiFiExtenderTheme {

                MainScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onChangeHotspotName = { newName ->
                        viewModel.handleUiActions(MainUiAction.OnSetLoading(true))
                        val prefs = getSharedPreferences(
                            ProxyService.PREF_NAME,
                            Context.MODE_PRIVATE
                        ).edit()
                        prefs.putString(ProxyService.KEY_NAME, newName)
                        prefs.apply()
                        restartHotSpot()
                    },
                    onChangeHotspotPassword = { newPassword ->
                        viewModel.handleUiActions(MainUiAction.OnSetLoading(true))
                        val prefs = getSharedPreferences(
                            ProxyService.PREF_NAME,
                            Context.MODE_PRIVATE
                        ).edit()
                        prefs.putString(ProxyService.KEY_PASSWORD, newPassword)
                        prefs.apply()
                        restartHotSpot()
                    },
                    onConnect = {
                        viewModel.handleUiActions(MainUiAction.OnSetLoading(true))
                        if (viewModel.uiState.value.isConnected) {
                            stopVpnService()
                        } else {
                            startVpnService()
                        }
                    },
                    onToggleHotspot = {
                        viewModel.handleUiActions(MainUiAction.OnSetLoading(true))
                        if (viewModel.uiState.value.isHotspotStarted) {
                            stopHotspot()
                        } else {
                            startHotspotWithPermissionRequest()
                        }
                    },
                    onToggleKeepScreenOn = {
                        viewModel.handleUiActions(MainUiAction.OnSetLoading(true))
                        if (viewModel.uiState.value.isKeepScreenOn) {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            viewModel.handleUiActions(
                                MainUiAction.OnUpdateKeepScreenOnFlag(false),
                                MainUiAction.OnSetLoading(false)
                            )
                        } else {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            viewModel.handleUiActions(
                                MainUiAction.OnUpdateKeepScreenOnFlag(true),
                                MainUiAction.OnSetLoading(false)
                            )
                        }
                    },
                    onToggleWPS = { /*TODO*/ }
                )
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allPermissionsGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            allPermissionsGranted =
                allPermissionsGranted && permissions[Manifest.permission.NEARBY_WIFI_DEVICES] == true
        }
        if (allPermissionsGranted) {
            startHotspot()
        } else {
            Toast.makeText(
                this,
                getString(R.string.permission_denied_location_is_required_to_discover_devices),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startHotspotWithPermissionRequest() {
        var granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            granted = granted && ContextCompat.checkSelfPermission(
                this, Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        if (granted) {
            startHotspot()
        } else {
            requestPermissionsLauncher.launch(
                perms.toTypedArray()
            )
        }
    }

    private fun startHotspot() {
        Intent(applicationContext, ProxyService::class.java).also {
            it.action = ProxyService.Actions.START.toString()
            startService(it)
        }
    }

    private fun stopHotspot() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            Intent(applicationContext, ProxyService::class.java).also {
                it.action = ProxyService.Actions.STOP.toString()
                startService(it)
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.wifi_must_be_enabled_for_the_app_to_work),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun restartHotSpot() {
        restartHotSpotAfterStop = true
        stopHotspot()
    }

    override fun onResume() {
        super.onResume()
        val mainApplication = (application as? MainApplication)
        onNewHotSpot(mainApplication?.hotspot)
        onNewVpnState(mainApplication?.isVpnStarted)
        mainApplication?.setHotSpotListener(this)
        mainApplication?.setVpnListener(this)

    }

    override fun onPause() {
        super.onPause()
        (application as? MainApplication)?.removeHotSpotListener()
        (application as? MainApplication)?.removeVpnListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onNewHotSpot(hotSpot: HotSpot?) {
        Log.d(TAG, "onNewHotSpot: $hotSpot")
        viewModel.handleUiActions(MainUiAction.OnSetHotspotName(hotSpot?.name ?: ""))
        viewModel.handleUiActions(MainUiAction.OnSetHotspotPassword(hotSpot?.password ?: ""))
        hotSpot?.let {
            viewModel.handleUiActions(
                MainUiAction.OnSetHotspotName(hotSpot.name),
                MainUiAction.OnSetHotspotPassword(hotSpot.password),
                MainUiAction.OnUpdateHotspotFlag(true),
                MainUiAction.OnSetLoading(false)
            )
        } ?: run {
            viewModel.handleUiActions(
                MainUiAction.OnSetHotspotName(""),
                MainUiAction.OnSetHotspotPassword(""),
                MainUiAction.OnUpdateHotspotFlag(false),
                MainUiAction.OnSetLoading(false)
            )
            if (restartHotSpotAfterStop) {
                restartHotSpotAfterStop = false
                Handler(Looper.getMainLooper()).postDelayed({
                    startHotspotWithPermissionRequest()
                }, 100)
            }
        }
    }

    override fun onNewDeviceList(new: WifiP2pGroup?) {
        Log.d(TAG, "New Device List: ${new?.clientList}")
        viewModel.handleUiActions(
            MainUiAction.OnSetConnectedDevices(new?.clientList?.toList() ?: emptyList())
        )
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(MyVpnService.TAG, "VPN permission granted")
            Intent(this, MyVpnService::class.java).also {
                it.action = MyVpnService.START_ACTION
                startService(it)
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.vpn_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startVpnService() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            Log.d(MyVpnService.TAG, "VPN permission required")
            vpnPermissionLauncher.launch(intent)
        } else {
            Log.d(MyVpnService.TAG, "VPN permission already granted")
            Intent(this, MyVpnService::class.java).also {
                it.action = MyVpnService.START_ACTION
                startService(it)
            }
        }
    }

    private fun stopVpnService() {
        Intent(this, MyVpnService::class.java).also {
            it.action = MyVpnService.STOP_ACTION
            startService(it)
        }
    }

    override fun onNewVpnState(isStarted: Boolean?) {
        if (isStarted == true) {
            viewModel.handleUiActions(
                MainUiAction.OnUpdateConnectedFlag(true),
                MainUiAction.OnSetLoading(false)
            )
        } else {
            viewModel.handleUiActions(
                MainUiAction.OnUpdateConnectedFlag(false),
                MainUiAction.OnSetLoading(false)
            )
        }
    }
}