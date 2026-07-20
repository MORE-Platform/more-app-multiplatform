package io.redlink.more.app.android.activities.subcomponents

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.theme.MoreColors

@Composable
fun ReloadButton(
    modifier: Modifier = Modifier
) {
    val isLoading = remember { mutableStateOf(false) }
    SmallTextButton(
        text = getStringResource(id = R.string.reload_button),
        buttonColors = ButtonDefaults.buttonColors(),
        borderStroke = MoreColors.borderPrimary(isLoading.value),
        modifier = modifier
    ) {
        isLoading.value = true
        MoreApplication.shared!!.updateStudy()
        isLoading.value = false
    }
}