package io.redlink.more.more_app_mutliplatform.models

class NotificationFilterModel(list: List<NotificationFilterTypeModel> = listOf())
    : List<NotificationFilterTypeModel> by list {
    fun changeFilter(filter: String): NotificationFilterModel{
        val copy = mutableListOf<NotificationFilterTypeModel>()
        copy.addAll(this)

        val model = NotificationFilterTypeModel.createModel(filter)
        if(model ==  NotificationFilterTypeModel.ALL || model == null) {
            copy.clear()
        } else if(this.contains(model)) {
            copy.remove(model)
        } else {
            copy.add(model)
        }
        return NotificationFilterModel(copy)
    }

    fun contains(element: String): Boolean {
        if(element == NotificationFilterTypeModel.ALL.type) {
            return isEmpty()
        }
        return contains(NotificationFilterTypeModel.createModel(element))
    }
}