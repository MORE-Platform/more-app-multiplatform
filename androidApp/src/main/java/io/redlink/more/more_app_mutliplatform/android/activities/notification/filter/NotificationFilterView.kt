import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.NavigationScreen
import io.redlink.more.more_app_mutliplatform.android.activities.studyDetails.NotificationViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getString
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.HeaderDescription
import io.redlink.more.more_app_mutliplatform.android.shared_composables.HeaderTitle
import io.redlink.more.more_app_mutliplatform.android.shared_composables.IconInline
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

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