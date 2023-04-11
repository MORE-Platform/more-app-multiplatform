import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.redlink.more.more_app_mutliplatform.android.activities.consent.ConsentViewModel
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.NotificationViewModel
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.rounded.Done
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.consent.composables.ConsentButtons
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.Accordion
import io.redlink.more.more_app_mutliplatform.android.shared_composables.AccordionReadMore
import io.redlink.more.more_app_mutliplatform.android.shared_composables.IconInline
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MessageAlertDialog



@Composable
fun NotificationView(navController: NavController, viewModel: NotificationViewModel) {
    val context = LocalContext.current

    LazyColumn(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.9f)
    ) {

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