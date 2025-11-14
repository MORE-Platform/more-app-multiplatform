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
package io.redlink.more.app.android.activities.dashboard

import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource

enum class Views(val tabPosition: Int, val tabText: String) {
    SCHEDULE(tabPosition = 0, tabText = stringResource(R.string.more_main_tab_schedule)),
    MODULES(tabPosition = 1, tabText = stringResource(R.string.more_main_tab_observations));
}