package io.redlink.more.app.android.activities.notification.filter


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.app.android.activities.notification.NotificationViewModel
import io.redlink.more.app.android.extensions.getString
import io.redlink.more.app.android.shared_composables.HeaderTitle
import io.redlink.more.app.android.shared_composables.MoreDivider
import io.redlink.more.app.android.R

@Composable
fun NotificationFilterView(viewModel: NotificationFilterViewModel, notificationViewModel: NotificationViewModel) {

    LazyColumn() {
        item {
            HeaderTitle(
                title = getString(R.string.more_select_filter),
                modifier = Modifier.padding(top = 20.dp)
            )
            MoreDivider(modifier = Modifier.padding(vertical = 10.dp))
        }
    }

}