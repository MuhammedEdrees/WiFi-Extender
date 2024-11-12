package org.codeslu.wifiextender.ui.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.codeslu.wifiextender.data.model.ConnectedDevice

@Composable
fun ConnectedDevicesSection(
    modifier: Modifier = Modifier,
    devices: List<ConnectedDevice>,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        devices.forEachIndexed { index, connectedDevice ->
            ConnectedDeviceItem(
                deviceName = connectedDevice.name,
                deviceIp = connectedDevice.ip,
                isLast = index == devices.size - 1
            )
        }
    }

}