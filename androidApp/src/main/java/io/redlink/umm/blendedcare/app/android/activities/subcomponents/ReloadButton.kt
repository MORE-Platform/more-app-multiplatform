package io.redlink.umm.blendedcare.app.android.activities.subcomponents

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.extensions.getStringResource
import io.redlink.umm.blendedcare.app.android.shared_composables.SmallTextButton
import io.redlink.umm.blendedcare.app.android.theme.MoreColors

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
        BlendedCareApplication.shared!!.updateStudyAsync()
        isLoading.value = false
    }
}