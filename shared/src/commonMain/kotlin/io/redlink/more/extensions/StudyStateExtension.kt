package io.redlink.more.extensions

import io.redlink.more.models.StudyState
import io.redlink.more.services.network.openapi.model.Study

fun Study.StudyState.toStudyState() =
    StudyState.getState(this.value)