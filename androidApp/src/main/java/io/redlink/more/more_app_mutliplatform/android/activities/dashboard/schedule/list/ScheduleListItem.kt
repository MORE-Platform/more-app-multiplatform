package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.minuteDiff
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.android.shared_composables.BasicText
import io.redlink.more.more_app_mutliplatform.android.shared_composables.MoreDivider
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTextButton
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTitle
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import java.util.Date

@Composable
fun ScheduleListItem(scheduleModel: ScheduleModel) {
    val enabled = scheduleModel.start.toDate() <= Date() && Date() < scheduleModel.end.toDate()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        SmallTitle(text = scheduleModel.observationTitle)
        BasicText(text = scheduleModel.observationType)
        ScheduleListItemTimeView(startTime = scheduleModel.start.toDate(), activeFor = scheduleModel.start.minuteDiff(scheduleModel.end).toInt())
        SmallTextButton(text = getStringResource(id = R.string.more_observation_start), enabled = enabled) {

        }
    }
}