package io.redlink.more.app.android.activities.bluetooth_conntection_view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.Heading
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun BluetoothConnectionView(viewModel: BluetoothConnectionViewModel) {
    LaunchedEffect(Unit) {
        viewModel.viewDidAppear()
    }
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Heading(
                text = getStringResource(id = R.string.more_ble_connected_devices),
                modifier = Modifier.fillMaxWidth()
            )
        }
        itemsIndexed(viewModel.connectedDevices) { _, device ->
            Column(modifier = Modifier.fillMaxWidth()) {
                MoreDivider()
                SmallTitle(text = device.deviceName)
                BasicText(text = device.address)
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Heading(
                    text = getStringResource(id = R.string.more_ble_discovered_devices)
                )
                if (viewModel.bluetoothIsScanning.value) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MoreColors.Primary
                    )
                    BasicText(text = "${getStringResource(id = R.string.more_ble_searching)}...")
                } else {
                    Box(modifier = Modifier)
                }
            }
        }
        itemsIndexed(viewModel.discoveredDevices){ _, device ->
            Column(modifier = Modifier.fillMaxWidth()) {
                MoreDivider()
                SmallTitle(text = device.deviceName)
                BasicText(text = device.address)
            }
        }
    }
}