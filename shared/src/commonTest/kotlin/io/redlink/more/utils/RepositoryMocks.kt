package io.redlink.more.utils

import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.database.repository.ScheduleRepository
import io.redlink.more.observations.ObservationDataManager
import io.redlink.more.observations.ObservationFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockScheduleRepository : ScheduleRepository(mockAppDatabase()) {
    var scheduleWithIdResult: Flow<ScheduleEntity?> = flowOf(null)
    var firstScheduleAvailableForObservationIdResult: Flow<ScheduleEntity?> = flowOf(null)

    override fun scheduleWithId(id: String): Flow<ScheduleEntity?> = scheduleWithIdResult

    override fun firstScheduleAvailableForObservationId(observationId: String): Flow<ScheduleEntity?> =
        firstScheduleAvailableForObservationIdResult
}

class MockMainRepository : MainRepository(mockAppDatabase()) {
    private val _mockSchedule = MockScheduleRepository()
    override val schedule: ScheduleRepository
        get() = _mockSchedule

    val mockSchedule: MockScheduleRepository
        get() = _mockSchedule
}

class MockObservationFactory(
    repository: MainRepository = MockMainRepository(),
    dataManager: ObservationDataManager? = null
) : ObservationFactory(repository, dataManager ?: mockObservationDataManager(repository)) {
    var matchingObservationTypes: Set<String> = emptySet()

    override fun getMatchingObservationTypes(types: Set<String>): Set<String> =
        matchingObservationTypes
}
