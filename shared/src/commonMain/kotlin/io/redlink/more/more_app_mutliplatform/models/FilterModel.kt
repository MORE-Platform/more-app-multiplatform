package io.redlink.more.more_app_mutliplatform.models

data class FilterModel(
    var dateFilter: DateFilterModel? = null,
    val observationTypeFilter: MutableSet<String> = mutableSetOf()
) {
    fun hasObservationTypeFilter() = this.observationTypeFilter.isNotEmpty()
}