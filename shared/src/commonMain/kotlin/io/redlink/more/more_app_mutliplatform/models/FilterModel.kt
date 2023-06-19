package io.redlink.more.more_app_mutliplatform.models

data class FilterModel(
    var dateFilter: Map<DateFilterModel, Boolean> = DateFilterModel.values()
        .associateWith { it == DateFilterModel.ENTIRE_TIME },
    var typeFilter: Map<String, Boolean> = emptyMap()
)
