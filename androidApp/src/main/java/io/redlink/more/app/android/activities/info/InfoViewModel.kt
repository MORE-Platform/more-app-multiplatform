/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.app.android.activities.info

import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.navigation.model.NavigationRoute
import io.redlink.more.viewModels.studydetails.CoreStudyDetailsViewModel

class InfoViewModel : ViewModel() {
    val coreViewModel =
        CoreStudyDetailsViewModel(MoreApplication.shared!!, NavigationRoute.INFO.viewIdentifier)
}