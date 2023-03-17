package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import kotlinx.coroutines.flow.MutableStateFlow

class TaskDetailsModel(
    val scheduleModel: ScheduleModel,
    val participantInformation: String,
    val dataPointCount: MutableStateFlow<DataPointCountSchema>
) {
    companion object {
        fun createModelsFrom(scheduleModel: ScheduleModel, participantInformation: String, count: DataPointCountSchema): TaskDetailsModel {
            return TaskDetailsModel(
                scheduleModel = scheduleModel,
                participantInformation = participantInformation,
                dataPointCount = MutableStateFlow(count)
            )
        }
    }
}