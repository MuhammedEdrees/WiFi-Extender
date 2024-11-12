package org.codeslu.wifiextender.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import org.codeslu.wifiextender.R
import org.codeslu.wifiextender.app.proxy.service.ProxyService
import org.codeslu.wifiextender.ui.theme.WiFiExtenderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
                0
            )
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = resources.getColor(R.color.background_dark),
            )
        )
        setContent {
            WiFiExtenderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            Intent(applicationContext, ProxyService::class.java).also {
                                it.action = ProxyService.Actions.START.toString()
                                startService(it)
                            }
                        }) {
                            Text(
                                text = "Start Proxy Server",
                            )
                        }
                        Button(onClick = {
                            Intent(applicationContext, ProxyService::class.java).also {
                                it.action = ProxyService.Actions.STOP.toString()
                                startService(it)
                            }
                        }) {
                            Text(
                                text = "Stop Proxy Server",
                            )
                        }

                    }
                }
            }
        }
    }

}