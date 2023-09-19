package io.redlink.more.app.android.activities.dashboard

import io.redlink.more.app.android.R
import io.redlink.more.app.android.extensions.stringResource

enum class Views(val tabPosition: Int, val tabText: String) {
    SCHEDULE(tabPosition = 0, tabText = stringResource(R.string.more_main_tab_schedule)),
    MODULES(tabPosition = 1, tabText = stringResource(R.string.more_main_tab_observations));
}