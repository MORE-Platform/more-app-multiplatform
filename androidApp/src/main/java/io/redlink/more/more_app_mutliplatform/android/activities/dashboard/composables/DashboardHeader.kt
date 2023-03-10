package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.composables

import androidx.compose.foundation.layout.*
import io.redlink.more.more_app_mutliplatform.android.R
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.DashboardViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.HeaderTitle
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

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