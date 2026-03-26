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
package io.redlink.more.models

import dev.icerock.moko.resources.StringResource
import io.redlink.more.SharedRes

enum class NotificationFilterTypeModel(val type: StringResource, val sortIndex: Int) {
    ALL(SharedRes.strings.more_filter_notification_all, 0),
    UNREAD(SharedRes.strings.more_filter_notification_unread, 1),
    IMPORTANT(SharedRes.strings.more_filter_notification_important, 2);

}