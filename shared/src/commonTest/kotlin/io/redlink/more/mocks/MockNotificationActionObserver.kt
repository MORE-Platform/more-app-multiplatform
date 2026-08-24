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
package io.redlink.more.mocks

import io.redlink.more.models.StudyState
import io.redlink.more.services.notification.NotificationActionObserver

class MockNotificationActionObserver : NotificationActionObserver {
    var updateStudyCalled = false
    var lastOldStudyState: StudyState? = null
    var lastNewStudyState: StudyState? = null

    override fun updateStudy(oldStudyState: StudyState?, newStudyState: StudyState?) {
        updateStudyCalled = true
        lastOldStudyState = oldStudyState
        lastNewStudyState = newStudyState
    }
}
