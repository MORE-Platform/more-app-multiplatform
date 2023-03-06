package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.ktor.utils.io.core.*
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class CoreObservationListModel {

    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    fun listSchedules(): List<ScheduleSchema> {
        val schedules: MutableList<ScheduleSchema> = mutableListOf()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            scheduleRepository.listSchedules().collect { scheduleList ->
                scheduleList.forEach { schedule ->
                    schedules.add(schedule)
                }
            }
        }
        return schedules
    }

    fun listDatesWithSchedules(): Map<String, List<ScheduleSchema>> {
        val datesWithSchedules: MutableSet<LocalDate?> = mutableSetOf()
        val schedules: List<ScheduleSchema> = listSchedules()
        val datesAndSchedulesMap: MutableMap<String, List<ScheduleSchema>> = mutableMapOf()
        listSchedules().forEach {
            val dateTime: LocalDateTime? =
                it.start?.toInstant()?.toLocalDateTime(TimeZone.currentSystemDefault())
            datesWithSchedules.add(dateTime?.date)
        }
        datesWithSchedules.forEach { date ->
            val schedulesOnDate: List<ScheduleSchema> = schedules.filter {
                it.start?.toInstant()?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == date
            }
            datesAndSchedulesMap[date.toString()] = schedulesOnDate
        }
        return datesAndSchedulesMap
    }
}