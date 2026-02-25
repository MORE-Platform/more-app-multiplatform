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
package io.redlink.umm.blendedcare.app.android.activities.info

import androidx.lifecycle.ViewModel
import io.redlink.umm.blendedcare.app.android.MoreApplication
import io.redlink.umm.participant.viewModels.studydetails.CoreStudyDetailsViewModel

class InfoViewModel : ViewModel() {
    val coreViewModel = CoreStudyDetailsViewModel(MoreApplication.shared!!)
}