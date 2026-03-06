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
