package org.codeslu.wifiextender.app.proxy.server

import android.util.Log
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import java.net.InetSocketAddress

class ProxyServer(private val port: Int, private val address: String) {
    private lateinit var proxyServer: HttpProxyServer

    companion object {
        private val TAG = "ProxyServer"
    }

    fun startProxy() {
        try {
            proxyServer = DefaultHttpProxyServer.bootstrap()
                .withAddress(InetSocketAddress(address, port))
                .withAllowLocalOnly(false)
                .start()
            Log.d(TAG, "Proxy server started at $address:$port")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error starting proxy server: ${e.message}")
        }
    }

    fun stopProxy() {
        if (::proxyServer.isInitialized) {
            proxyServer.stop()
            Log.d(TAG, "Proxy server stopped.")
        }
    }
}

