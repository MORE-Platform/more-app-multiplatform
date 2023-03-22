package io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.more_app_mutliplatform.android.extensions.getStringResource
import io.redlink.more.more_app_mutliplatform.android.extensions.toDate
import io.redlink.more.more_app_mutliplatform.android.shared_composables.BasicText
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTextButton
import io.redlink.more.more_app_mutliplatform.android.shared_composables.SmallTitle
import io.redlink.more.more_app_mutliplatform.android.ui.theme.MoreColors
import io.redlink.more.more_app_mutliplatform.models.ScheduleModel
import io.redlink.more.more_app_mutliplatform.viewModels.schedules.ScheduleState
import java.util.*

@Composable
fun ScheduleListItem(scheduleModel: ScheduleModel, viewModel: ScheduleViewModel) {
    val context = LocalContext.current
    val enabled = scheduleModel.start.toDate() <= Date() && Date() < scheduleModel.end.toDate()
    val currentState = viewModel.activeScheduleState[scheduleModel.scheduleId] ?: ScheduleState.NON
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        SmallTitle(text = scheduleModel.observationTitle, color = MoreColors.Primary)
        BasicText(text = scheduleModel.observationType, color = MoreColors.Secondary)
        ScheduleListItemTimeView(
            startTime = scheduleModel.start.toDate(),
            endTime = scheduleModel.end.toDate()
        )
        SmallTextButton(
            text = if (currentState == ScheduleState.RUNNING) getStringResource(id = R.string.more_observation_pause) else getStringResource(
                id = R.string.more_observation_start
            ), enabled = enabled
        ) {
            if (currentState == ScheduleState.RUNNING) {
                viewModel.pauseObservation(context, scheduleModel.scheduleId)
            } else {
                viewModel.startObservation(
                    context,
                    scheduleModel.scheduleId
                )
            }
        }
    }
}