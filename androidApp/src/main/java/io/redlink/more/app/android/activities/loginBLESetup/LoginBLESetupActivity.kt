package io.redlink.more.app.android.activities.loginBLESetup

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.EmptyListView
import io.redlink.more.app.android.shared_composables.Heading
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.shared_composables.SmallTitle
import io.redlink.more.app.android.shared_composables.Title
import io.redlink.more.app.android.ui.theme.MoreColors

class LoginBLESetupActivity : ComponentActivity() {
    val viewModel = LoginBLESetupViewModel(
        observationFactory = MoreApplication.observationFactory!!,
        bluetoothConnector = MoreApplication.androidBluetoothConnector!!
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginBLESetupView(viewModel)
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
}

@Composable
fun LoginBLESetupView(viewModel: LoginBLESetupViewModel) {
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
            }
            itemsIndexed(viewModel.neededDevices) {_, item ->
                BasicText(text = "- $item")
            }
            item {
                BasicText(text = getStringResource(id = R.string.more_ble_context_description_part2), modifier = Modifier.padding(vertical = 4.dp))
                Divider()
            }
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
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    MoreDivider()
                    SmallTitle(text = device.deviceName ?: getStringResource(id = R.string.more_ble_unknown_device))
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
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clickable {
                            viewModel.connectToDevice(device)
                        }
                ) {
                    MoreDivider()
                    SmallTitle(text = device.deviceName ?: getStringResource(id = R.string.more_ble_unknown_device))
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
                    if (viewModel.isScanning.value) {
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
}