package io.redlink.more.app.android.activities.bluetooth_conntection_view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.EmptyListView
import io.redlink.more.app.android.shared_composables.Heading
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun BluetoothConnectionView(navController: NavController, viewModel: BluetoothConnectionViewModel) {
    val progressSize = 20.dp
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.BLUETOOTH_CONNECTION.route)
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Heading(
                    text = getStringResource(id = R.string.more_ble_connected_devices)
                )
                Box(modifier = Modifier)

            }
        }
        item {
            if (viewModel.connectedDevices.isEmpty()) {
                EmptyListView(text = getStringResource(id = R.string.more_ble_no_connected))
            }
        }
        itemsIndexed(viewModel.connectedDevices) { _, device ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                MoreDivider()
                SmallTitle(text = device.deviceName ?: getStringResource(id = R.string.more_ble_unknown_device))
                BasicText(text = device.address ?: getStringResource(id = R.string.more_ble_unknown_device))
            }
        }
        item {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Heading(
                    text = getStringResource(id = R.string.more_ble_discovered_devices)
                )
            }
        }
        item {
            if (viewModel.discoveredDevices.isEmpty()) {
                EmptyListView(text = getStringResource(id = R.string.more_ble_no_discovered))
            }
        }
        itemsIndexed(viewModel.discoveredDevices){ _, device ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.connectToDevice(device)
                    }
            ) {
                MoreDivider()
                SmallTitle(text = device.deviceName ?: getStringResource(id = R.string.more_ble_unknown_device))
                BasicText(text = device.address ?: getStringResource(id = R.string.more_ble_unknown_device))
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                if (viewModel.bluetoothIsScanning.value) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MoreColors.Primary,
                        modifier = Modifier
                            .height(progressSize)
                            .width(progressSize)
                    )
                    BasicText(
                        text = "${getStringResource(id = R.string.more_ble_searching)}...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }
}