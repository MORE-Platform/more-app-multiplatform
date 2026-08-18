/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.app.android.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.redlink.more.app.android.activities.bluetooth.BLEConnectionActivity
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.activities.observations.garmin.GarminConnectActivity
import io.redlink.more.app.android.activities.observations.limeSurvey.LimeSurveyActivity
import io.redlink.more.app.android.activities.studyDetails.observationDetails.ObservationDetailsViewModel
import io.redlink.more.app.android.util.ActivityProvider
import io.redlink.more.models.ScheduleListType
import io.redlink.more.viewModels.ViewManager
import io.redlink.more.viewModels.notifications.CoreNotificationFilterViewModel
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val tabIndex = mutableIntStateOf(0)
    val showBackButton = mutableStateOf(false)
    val navigationBarTitle = mutableStateOf("")

    val coreNotificationFilterViewModel = CoreNotificationFilterViewModel()

    val manualTasks: ScheduleViewModel = ScheduleViewModel(
        ScheduleListType.MANUALS
    )

    val runningSchedulesViewModel: ScheduleViewModel by lazy {
        ScheduleViewModel(
            ScheduleListType.RUNNING
        )
    }
    val completedSchedulesViewModel: ScheduleViewModel by lazy {
        ScheduleViewModel(
            ScheduleListType.COMPLETED
        )
    }

    private var lastBleViewState = false

    init {
        viewModelScope.launch {
            ViewManager.bleViewActive.collect {
                if (it && !lastBleViewState) {
                    ActivityProvider.getCurrentActivity()?.let { activity ->
                        openBLESetupActivity(activity)
                    }
                }
                lastBleViewState = it
            }
        }
    }

    fun openLimesurvey(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>,
        scheduleId: String?,
        observationId: String?,
        notificationId: String?
    ) {
        (context as? Activity)?.let { activity ->
            val intent = Intent(activity, LimeSurveyActivity::class.java)
            intent.putExtra(
                LimeSurveyActivity.LIME_SURVEY_ACTIVITY_SCHEDULE_ID,
                scheduleId
            )
            intent.putExtra(
                LimeSurveyActivity.LIME_SURVEY_ACTIVITY_OBSERVATION_ID,
                observationId
            )
            intent.putExtra(
                LimeSurveyActivity.LIME_SURVEY_ACTIVITY_NOTIFICATION_ID,
                notificationId
            )
            activityResultLauncher.launch(intent)
        }
    }

    fun openGarminActivity(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        (context as? Activity)?.let { activity ->
            val intent = Intent(activity, GarminConnectActivity::class.java)
            activityResultLauncher.launch(intent)
        }
    }

    fun createObservationDetailView(observationId: String): ObservationDetailsViewModel {
        return ObservationDetailsViewModel(observationId)
    }

    fun schedulesViewModel(type: ScheduleListType): ScheduleViewModel = when (type) {
        ScheduleListType.MANUALS -> manualTasks
        ScheduleListType.RUNNING -> runningSchedulesViewModel
        ScheduleListType.COMPLETED -> completedSchedulesViewModel
        ScheduleListType.ALL -> ScheduleViewModel(ScheduleListType.ALL)
    }

    private fun openBLESetupActivity(context: Context) {
        (context as? Activity)?.let {
            val intent = Intent(context, BLEConnectionActivity::class.java)
            intent.putExtra(BLEConnectionActivity.SHOW_DESCR_PART2, true)
            it.startActivity(intent)
        }
    }
}
