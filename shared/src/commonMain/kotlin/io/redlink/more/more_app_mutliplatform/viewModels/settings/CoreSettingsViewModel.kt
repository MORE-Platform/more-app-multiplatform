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
package io.redlink.more.more_app_mutliplatform.viewModels.settings

import io.ktor.utils.io.core.Closeable
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.entities.ObservationEntity
import io.redlink.more.more_app_mutliplatform.database.entities.StudyEntity
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.PermissionModel
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine

class CoreSettingsViewModel(
    private val shared: Shared
) : CoreViewModel() {
    val dataDeleted = MutableStateFlow(false)

    val study = MutableStateFlow<StudyEntity?>(null)
    val observations = MutableStateFlow(emptyList<ObservationEntity>())

    val permissionModel = MutableStateFlow<PermissionModel?>(null)

    override fun viewDidAppear() {
        launchScope {
            shared.repositories.study.study.combine(shared.repositories.observation.observations()) { study, observations ->
                Pair(study, observations)
            }.cancellable().collect {
                if (it.first?.active == true) {
                    study.value = it.first
                    it.first?.let { study ->
                        permissionModel.value =
                            PermissionModel.createFromSchema(study, it.second)
                    }
                }
            }
        }
        launchScope {
            shared.repositories.observation.observations().cancellable().collect {
                observations.value = it
            }
        }
    }

    fun onLoadStudy(provideNewState: ((StudyEntity?) -> Unit)): Closeable {
        return study.asClosure(provideNewState)
    }

    fun onPermissionChange(provideNewState: (PermissionModel?) -> Unit): Closeable {
        return permissionModel.asClosure(provideNewState)
    }

    fun exitStudy() {
        shared.exitStudy {
            dataDeleted.value = true
        }
    }

}