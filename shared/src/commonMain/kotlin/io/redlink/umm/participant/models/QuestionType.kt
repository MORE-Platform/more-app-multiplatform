package io.redlink.umm.participant.models

enum class QuestionType(val observationType: String, val observationDataResponseKey: String) {
    NON("", ""),
    SINGLE_CHOICE("question-observation", "answer"),
    MULTIPLE_CHOICE("multiple-choice-question-observation", "answers");

    companion object {
        fun questionTypeForObservationType(observationType: String) =
            entries.firstOrNull { it.observationType == observationType } ?: NON
    }
}