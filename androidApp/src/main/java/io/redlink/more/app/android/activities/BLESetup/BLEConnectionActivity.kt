package io.redlink.more.app.android.activities.BLESetup

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.EmptyListView
import io.redlink.more.app.android.shared_composables.Heading
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.shared_composables.Title
import io.redlink.more.app.android.ui.theme.MoreColors

class BLEConnectionActivity : ComponentActivity() {
    val viewModel = BLESetupViewModel(
        observationFactory = MoreApplication.shared!!.observationFactory,
        bluetoothConnector = MoreApplication.shared!!.mainBluetoothConnector
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val showDescrPart2 = intent.getBooleanExtra(SHOW_DESCR_PART2, false)
        setContent {
            LoginBLESetupView(viewModel, showDescrPart2)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.viewDidAppear()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.viewDidDisappear()
    }

    companion object {
        const val SHOW_DESCR_PART2 = "bleActivityShowDescrPart2"
    }
}

@Composable
fun LoginBLESetupView(viewModel: BLESetupViewModel, showDescrPart2: Boolean) {
    val context = LocalContext.current
    val progressSize = 20.dp
    MoreBackground(
        showBackButton = true,
        onBackButtonClick = {
            (context as? Activity)?.finish()
        }
    ) {
        LazyColumn {
            item {
                Title(text = getStringResource(id = R.string.more_ble_setup_title))
                BasicText(text = "${getStringResource(id = R.string.more_ble_context_description)}:", modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            itemsIndexed(viewModel.neededDevices) {_, item ->
                SmallTitle(text = "- $item", fontSize = 16.sp, color = MoreColors.PrimaryDark)
            }
            if (showDescrPart2) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    BasicText(text = getStringResource(id = R.string.more_ble_context_description_part2), modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MoreDivider()
                Spacer(modifier = Modifier.height(12.dp))
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
            if (viewModel.bluetoothPowerState.value) {
                if (viewModel.connectedDevices.isEmpty()) {
                    item {
                        EmptyListView(text = getStringResource(id = R.string.more_ble_no_connected))
                    }
                } else {
                    itemsIndexed(viewModel.connectedDevices) { _, device ->
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable {
                                    viewModel.disconnectFromDevice(device)
                                }
                        ) {
                            MoreDivider()
                            SmallTitle(text = device.deviceName ?: getStringResource(id = R.string.more_ble_unknown_device))
                        }
                    }
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MoreDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Heading(
                            text = getStringResource(id = R.string.more_ble_discovered_devices)
                        )
                        if (viewModel.connectedDevices.isEmpty()) {
                            BasicText(
                                text = "${getStringResource(id = R.string.more_connect_device_info)}",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
                if (viewModel.discoveredDevices.isEmpty()) {
                    item {
                        EmptyListView(text = getStringResource(id = R.string.more_ble_no_discovered))
                    }
                } else {
                    itemsIndexed(viewModel.discoveredDevices){ _, device ->
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable {
                                    viewModel.connectToDevice(device)
                                }
                        ) {
                            MoreDivider()
                            SmallTitle(text = device.deviceName ?: getStringResource(id = R.string.more_ble_unknown_device))
                        }
                    }
                }
                if (viewModel.isScanning.value) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
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
            } else {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.BluetoothDisabled, contentDescription = getStringResource(id = R.string.more_ble_disabled), tint = MoreColors.Important)
                            SmallTitle(text = getStringResource(id = R.string.more_ble_disabled), color = MoreColors.Important)
                        }
                    }
                }
            }
        }

    }
}