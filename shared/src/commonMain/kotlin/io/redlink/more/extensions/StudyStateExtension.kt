package io.redlink.more.extensions

import io.redlink.more.models.StudyState
import io.redlink.more.services.network.openapi.model.Study

fun io.redlink.more.model.Study.StudyState.toStudyState() =
    StudyState.Companion.getState(this.value)