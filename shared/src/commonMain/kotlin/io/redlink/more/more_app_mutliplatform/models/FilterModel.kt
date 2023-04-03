package io.redlink.more.more_app_mutliplatform.models

data class FilterModel(
    var dateFilter: DateFilterModel = DateFilterModel.ENTIRE_TIME,
    val typeFilter: MutableSet<String> = mutableSetOf()
)