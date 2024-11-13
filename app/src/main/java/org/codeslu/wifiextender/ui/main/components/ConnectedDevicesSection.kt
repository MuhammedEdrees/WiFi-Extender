package org.codeslu.wifiextender.ui.main.components

import android.net.wifi.p2p.WifiP2pDevice
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConnectedDevicesSection(
    modifier: Modifier = Modifier,
    devices: Collection<WifiP2pDevice>,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        devices.forEachIndexed { index, connectedDevice ->
            ConnectedDeviceItem(
                deviceName = connectedDevice.deviceName,
                deviceAddress = connectedDevice.deviceAddress,
                isLast = index == devices.size - 1
            )
        }
    }

}