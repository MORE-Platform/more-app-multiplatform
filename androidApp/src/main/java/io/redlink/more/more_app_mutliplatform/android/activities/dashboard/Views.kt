package io.redlink.more.more_app_mutliplatform.android.activities.dashboard

import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.extensions.getString

enum class Views(val tabPosition: Int, val tabText: String) {
    SCHEDULE(tabPosition = 0, tabText = getString(R.string.more_main_tab_schedule)),
    MODULES(tabPosition = 1, tabText = getString(R.string.more_main_tab_modules));
}