package io.redlink.more.more_app_mutliplatform.models

enum class NotificationFilterTypeModel(val type: String?) {
    UNREAD("unread"),
    IMPORTANT("important");

    companion object {
        fun createModel(type: String?): NotificationFilterTypeModel? {
            return when(type) {
                UNREAD.type -> UNREAD
                IMPORTANT.type -> IMPORTANT
                else -> { null }
            }
        }
    }
}