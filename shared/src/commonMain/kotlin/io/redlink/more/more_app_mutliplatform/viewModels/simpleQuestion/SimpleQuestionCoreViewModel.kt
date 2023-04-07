package io.redlink.more.more_app_mutliplatform.viewModels.simpleQuestion

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.database.repository.ScheduleRepository
import io.redlink.more.more_app_mutliplatform.models.TaskDetailsModel
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation.SimpleQuestionObservation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class SimpleQuestionCoreViewModel(
    private val scheduleId: String,
    private val observation: Observation
) {
    private val scheduleRepository: ScheduleRepository = ScheduleRepository()
    private val observationRepository: ObservationRepository = ObservationRepository()

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    val taskDetailsModel = MutableStateFlow<TaskDetailsModel?>(null)
    val question = MutableStateFlow("")
    val answers = MutableStateFlow(setOf<String>())
    val hasQuestionType = observation is SimpleQuestionObservation


    init {
        if (hasQuestionType)
            scope.launch {
                scheduleRepository.scheduleWithId(scheduleId).collect { schedule ->
                    schedule?.let { schedule ->
                        observationRepository.observationById(schedule.observationId).firstOrNull()
                            ?.let { observationSchema ->
                                taskDetailsModel.emit(
                                    TaskDetailsModel.createModelFrom(
                                        observationSchema,
                                        schedule,
                                        0
                                    )
                                )

                                val config: Map<String, Any> =
                                    observationSchema.configuration?.let { config ->
                                        try {
                                            Json.decodeFromString<JsonObject>(config).toMap()
                                        } catch (e: Exception) {
                                            Napier.e { e.stackTraceToString() }
                                            emptyMap()
                                        }
                                    } ?: emptyMap()

                                question.value = config["question"].toString()
                                answers.value =
                                    (config["answers"] as? Array<*>)?.filterIsInstance<String>()
                                        ?.toSet() ?: emptySet()
                            }
                    }
                }
            }
    }

    fun finishQuestion(data: String){
        observation.storeData(data)
        taskDetailsModel.value?.let { observation.stop(it.observationId) }
    }
}
