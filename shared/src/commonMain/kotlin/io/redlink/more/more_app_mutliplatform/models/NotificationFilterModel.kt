package io.redlink.more.more_app_mutliplatform.models

class NotificationFilterModel()
    : MutableList<NotificationFilterTypeModel> by mutableListOf() {
    fun changeFilter(filter: String): NotificationFilterModel{
        val model = NotificationFilterTypeModel.createModel(filter)
        if(model ==  NotificationFilterTypeModel.ALL || model == null) {
            this.clear()
        } else if(this.contains(model)) {
            this.remove(model)
        } else {
            this.add(model)
        }
        val copy = NotificationFilterModel()
        copy.addAll(this)
        return copy
    }

    fun contains(element: String): Boolean {
        if(element == NotificationFilterTypeModel.ALL.type) {
            return isEmpty()
        }
        return contains(NotificationFilterTypeModel.createModel(element))
    }
}