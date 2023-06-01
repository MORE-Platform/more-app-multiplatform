package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.ktor.utils.io.core.*
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.asMappedFlow
import io.redlink.more.more_app_mutliplatform.extensions.toInstant
import io.redlink.more.more_app_mutliplatform.models.ScheduleState
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.util.Scope.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.mongodb.kbson.ObjectId

class ScheduleRepository : Repository<ScheduleSchema>() {

    override fun count(): Flow<Long> = realmDatabase().count<ScheduleSchema>()

    fun allSchedulesWithStatus(done: Boolean = false): Flow<List<ScheduleSchema>> {
        return realm()?.query<ScheduleSchema>("done = $0", done)?.asMappedFlow() ?: emptyFlow()
    }

    fun allScheduleWithRunningState(scheduleState: ScheduleState = ScheduleState.RUNNING) =
        realmDatabase().query<ScheduleSchema>(
            query = "state = $0",
            queryArgs = arrayOf(scheduleState.name)
        )

    fun collectRunningState(
        forState: ScheduleState,
        provideNewState: (List<ScheduleSchema>) -> Unit
    ): Closeable {
        return allScheduleWithRunningState(forState).asClosure(provideNewState)
    }

    fun getFirstAndLastDate(observationId: String): Flow<Pair<ScheduleSchema?, ScheduleSchema?>> {
        return realmDatabase().query<ScheduleSchema>(
            query = "observationId = $0",
            queryArgs = arrayOf(observationId)
        ).transform {
            val start = it.sortedBy { it.start }.firstOrNull()
            val end = it.sortedBy { it.end }.lastOrNull()

            emit(Pair(start, end))
        }
    }

    fun setRunningStateFor(id: String, scheduleState: ScheduleState) {
        realm()?.writeBlocking {
            this.query<ScheduleSchema>("scheduleId = $0", ObjectId(id)).first().find()?.let {
                it.state = scheduleState.name
            }
        }
    }

    fun setCompletionStateFor(id: String, wasDone: Boolean) {
        realm()?.writeBlocking {
            this.query<ScheduleSchema>("scheduleId = $0", ObjectId(id)).first().find()
                ?.updateState(if (wasDone) ScheduleState.DONE else ScheduleState.ENDED)
        }
    }

    fun getNextScheduleStart(): Flow<ScheduleSchema?> {
        return allSchedulesWithStatus().transform { schemas ->
            emit(schemas.filter { (it.start ?: RealmInstant.now()) > RealmInstant.now() }
                .sortedBy { it.start }.firstOrNull())
        }
    }

    fun getNextScheduleEnd(scheduleId: String): Flow<Long?> {
        return scheduleWithId(scheduleId).transform { emit(it?.end?.epochSeconds) }
    }

    fun nextSchedule(): Flow<Long?> {
        return allSchedulesWithStatus().transform { schemas ->
            val startInstances =
                schemas.mapNotNull { it.start }.filter { it > RealmInstant.now() }.toSet()
            val endInstances =
                schemas.mapNotNull { it.end }.filter { it > RealmInstant.now() }.toSet()
            val nextStart = startInstances.minOfOrNull { it.epochSeconds }
            val nextEnd = endInstances.minOfOrNull { it.epochSeconds }
            if (nextStart != null && nextEnd != null) {
                if (nextStart < nextEnd) emit(nextStart) else emit(nextEnd)
                return@transform
            }
            if (nextStart != null) emit(nextStart) else emit(nextEnd)
        }
    }

    suspend fun getNextSchedule() = nextSchedule().firstOrNull()

    fun nextScheduleStart(): Flow<Long?> {
        return allSchedulesWithStatus().transform { schemas ->
            emit(
                schemas.mapNotNull { it.start }.filter { it > RealmInstant.now() }.toSet()
                    .minOfOrNull { it.epochSeconds })
        }
    }

    fun collectNextScheduleStart(provideNewState: (Long?) -> Unit) =
        nextScheduleStart().asClosure(provideNewState)

    fun scheduleWithId(id: String) = realmDatabase().queryFirst<ScheduleSchema>(
        query = "scheduleId = $0",
        queryArgs = arrayOf(ObjectId(id))
    )

    fun updateTaskStates(observationFactory: ObservationFactory? = null) {
        launch {
            updateTaskStatesSync(observationFactory)
        }
    }

    suspend fun updateTaskStatesSync(observationFactory: ObservationFactory? = null) {
        val restartableTypes =
            observationFactory?.observationTypesNeedingRestartingAfterAppClosure() ?: emptySet()
        val now = Clock.System.now().epochSeconds
        Napier.d { "Updating Schedule states..." }
        realm()?.let {
            it.write {
                query<ScheduleSchema>("done = $0", false).find().forEach { scheduleSchema ->
                    if (restartableTypes.isNotEmpty()
                        && scheduleSchema.getState() == ScheduleState.RUNNING
                        && scheduleSchema.observationType in restartableTypes
                        && scheduleSchema.end?.let { it.epochSeconds > now } == true
                    ) {
                        Napier.i { "Resetting ${scheduleSchema.scheduleId}" }
                        scheduleSchema.updateState(ScheduleState.ACTIVE)
                    } else {
                        scheduleSchema.updateState()
                    }
                }
            }
        }
    }


}