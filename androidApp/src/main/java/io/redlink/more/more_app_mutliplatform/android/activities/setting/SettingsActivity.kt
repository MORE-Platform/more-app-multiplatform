package io.redlink.more.more_app_mutliplatform.android.activities.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.ContentView
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

class SettingsActivity: ComponentActivity() {

    private val viewModel = SettingsViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.createCoreViewModel(this)
        setContent {
            MoreBackground {
                SettingsView(viewModel = viewModel)
            }
        }
    }
}


@Composable
fun SettingsView(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    Column(

    ) {
        Text(
            text = "Settings",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MoreColors.MainTitle,
            maxLines = 2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.removeParticipation(context)
            }
        ){
            Text(
                text = getStringResource(id = R.string.more_settings_exit_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MoreColors.MainTitle,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}