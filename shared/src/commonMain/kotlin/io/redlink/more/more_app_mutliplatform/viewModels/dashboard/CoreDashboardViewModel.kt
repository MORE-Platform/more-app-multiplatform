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
package io.redlink.more.more_app_mutliplatform.viewModels.dashboard

import io.redlink.more.more_app_mutliplatform.database.AppDatabase
import io.redlink.more.more_app_mutliplatform.database.entities.StudyEntity
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.viewModels.CoreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable

class CoreDashboardViewModel(database: AppDatabase) : CoreViewModel() {
    private val studyRepository: StudyRepository = StudyRepository(database)
    val study: MutableStateFlow<StudyEntity?> = MutableStateFlow(null)

    override fun viewDidAppear() {
        launchScope {
            studyRepository.getStudy().cancellable().collect {
                study.value = it
            }
        }
    }

    fun onLoadStudy(provideNewState: ((StudyEntity?) -> Unit)) = study.asClosure(provideNewState)
}