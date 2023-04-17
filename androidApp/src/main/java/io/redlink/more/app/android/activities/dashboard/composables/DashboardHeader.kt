package io.redlink.more.app.android.activities.dashboard.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.redlink.more.app.android.activities.dashboard.DashboardViewModel
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.R


@Composable
fun DashboardHeader(model: DashboardViewModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
        )
    {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ){
            HeaderTitle(title = model.studyTitle.value)
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = getStringResource(id = R.string.study_details),
                tint = MoreColors.Primary,
                modifier = Modifier
            )
        }

    }
}