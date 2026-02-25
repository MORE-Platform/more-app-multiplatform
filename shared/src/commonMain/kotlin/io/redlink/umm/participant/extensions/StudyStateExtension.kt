package io.redlink.umm.participant.extensions

import io.redlink.umm.blendedcare.services.network.openapi.model.Study
import io.redlink.umm.participant.models.StudyState

fun Study.StudyState.toStudyState() = StudyState.getState(this.value)