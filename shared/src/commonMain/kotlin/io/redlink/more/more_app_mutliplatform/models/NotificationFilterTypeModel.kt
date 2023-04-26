package io.redlink.more.more_app_mutliplatform.models

enum class NotificationFilterTypeModel(val type: String) {
    ALL("all"),
    UNREAD("unread"),
    IMPORTANT("important");

    companion object {
        fun createModel(type: String): NotificationFilterTypeModel? {
            return when(type) {
                ALL.type -> ALL
                UNREAD.type -> UNREAD
                IMPORTANT.type -> IMPORTANT
                else -> { null }
            }
        }
    }
}