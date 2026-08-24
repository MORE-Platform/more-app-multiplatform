/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.models

enum class QuestionType(val observationType: String, val observationDataResponseKey: String) {
    NON("", ""),
    SINGLE_CHOICE("question-observation", "answer"),
    MULTIPLE_CHOICE("multiple-choice-question-observation", "answers");

    companion object {
        fun questionTypeForObservationType(observationType: String) =
            entries.firstOrNull { it.observationType == observationType } ?: NON
    }
}