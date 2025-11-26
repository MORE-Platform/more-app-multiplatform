package io.redlink.umm.blendedcare.app.android.activities.observationErrors

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.activities.NavigationScreen
import io.redlink.umm.blendedcare.app.android.activities.bluetooth.BLEConnectionActivity
import io.redlink.umm.blendedcare.app.android.extensions.getStringResource
import io.redlink.umm.blendedcare.app.android.extensions.getStringResourceByName
import io.redlink.umm.blendedcare.app.android.extensions.showNewActivity
import io.redlink.umm.blendedcare.app.android.shared_composables.BasicText
import io.redlink.umm.blendedcare.app.android.shared_composables.SmallTextIconButton
import io.redlink.umm.blendedcare.app.android.theme.MoreColors
import io.redlink.umm.participant.observations.Observation

@Composable
fun ObservationErrorListView(
    errors: List<String>,
    errorActions: List<String>
) {
    val context = LocalContext.current
    if (errors.isNotEmpty()) {
        LazyColumn(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(errors.toList()) { error ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MoreColors.Companion.Important,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    BasicText(
                        text = "${getStringResourceByName(error)}!",
                        fontSize = 16.sp,
                        color = MoreColors.Companion.Important
                    )
                }
            }
            if (errorActions.isNotEmpty()) {
                item {
                    if (errorActions.contains(Observation.ERROR_DEVICE_NOT_CONNECTED)) {
                        SmallTextIconButton(
                            text = NavigationScreen.BLUETOOTH_CONNECTION.stringRes(),
                            imageText = getStringResource(id = R.string.more_ble_icon_description),
                            image = Icons.Default.Watch,
                            imageTint = MoreColors.Companion.White
                        ) {
                            (context as? Activity)?.let {
                                showNewActivity(it, BLEConnectionActivity::class.java)
                            }
                        }
                    }
                }
            }
        }
        Divider(thickness = 1.dp)
    }
}