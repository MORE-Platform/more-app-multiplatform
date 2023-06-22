package io.redlink.more.more_app_mutliplatform.models

enum class NotificationFilterTypeModel(val type: String, val sortIndex: Int) {
    ALL("All", 0),
    UNREAD("Unread", 1),
    IMPORTANT("Important", 2);

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