package io.redlink.more.more_app_mutliplatform.android.activities.notification.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen
import io.redlink.more.more_app_mutliplatform.android.activities.notification.NotificationViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun NotificationFilterViewButton(navController: NavController, viewModel: NotificationViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 19.dp)
            .clickable(onClick = { navController.navigate(NavigationScreen.NOTIFICATION_FILTER.route) })
    ){
        Text(
            text = viewModel.currentFilter,
            color = MoreColors.Primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
        )
        Icon(
            Icons.Default.Tune,
            contentDescription = getStringResource(id = R.string.more_main_tab_filters),
            tint = MoreColors.Secondary,
            modifier = Modifier
                .padding(start = 8.dp)
        )
    }
}