import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.Image
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.shared_composables.IconInline
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors

@Composable
fun NotificationItem(
    title: String?,
    titleColor: Color = MoreColors.Primary,
    body: String?,
    read: Boolean?,
    priority: Long?,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                if(priority?.toInt() == 2) {
                    Image(
                        id = R.drawable.warning_exclamation,
                        contentDescription = "More Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.06f)
                            .padding(top = 4.dp)
                    )
                    Spacer(modifier = if (read != null && read) Modifier.width(5.dp) else Modifier.width(8.dp))
                }
                Text(
                    text = title ?: "Notification",
                    fontWeight = if (read != null && read) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (priority?.toInt() == 2) MoreColors.Important else titleColor,
                    modifier = Modifier.fillMaxWidth(0.96f)
                )
            }

            if(read != null && !read) {
                    IconInline(
                        icon = Icons.Filled.Circle,
                        color = MoreColors.Important,
                        contentDescription = getStringResource(id = R.string.more_notification_view_show_unread),
                    )
            }
        }
        Divider()
        if (body != null) {
            Text(
                text = body,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = MoreColors.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (body.isNotEmpty()) 30.dp else 5.dp)
            )
        }
    }
}