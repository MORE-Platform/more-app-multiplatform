import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.notification.NotificationViewModel
import io.redlink.more.app.android.activities.notification.composables.NotificationFilterViewButton
import io.redlink.more.app.android.ui.theme.MoreColors


@Composable
fun NotificationView(navController: NavController, viewModel: NotificationViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.9f)
    ) {

        item() {
            Column(modifier = Modifier
                .height(IntrinsicSize.Min
                )) {
                NotificationFilterViewButton(navController, viewModel = viewModel)
            }
            Spacer(modifier = Modifier.padding(10.dp))
        }

        items(viewModel.notificationList) { notification ->
            NotificationItem(title = notification.title, message = notification.message, read = notification.read, isImportant = notification.isImportant)
        }
    }
}

fun setNotificationColor(isImportant: Boolean): Color {
    if (isImportant) {
        return MoreColors.Important
    }
    return MoreColors.Primary
}