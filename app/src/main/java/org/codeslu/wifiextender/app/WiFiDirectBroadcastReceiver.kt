package org.codeslu.wifiextender.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WiFiDirectBroadcastReceiver(private val listener: Listener): BroadcastReceiver() {
    interface Listener {
        fun onReceive(context: Context?, intent: Intent?)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        listener.onReceive(context, intent)
    }
}