package io.redlink.more.more_app_mutliplatform.android.activities.login.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.android.ui.theme.morePrimary

@Composable
fun QRCodeButton() {
    OutlinedButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(vertical = 8.dp)
            .height(50.dp),
        colors = ButtonDefaults.morePrimary(),
        border = MoreColors.borderPrimary(true)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = getStringResource(id = R.string.more_qr_code_button))
            Icon(
                Icons.Default.ArrowForwardIos,
                tint = MoreColors.White,
                contentDescription = "Scan QR Code",
                modifier = Modifier.fillMaxHeight(0.4f)
            )
        }
    }
}