import androidx.lifecycle.ViewModel

data class NotificationFitler (
    val title: String,
    val value: String,
    var selected: Boolean
)

class NotificationFilterViewModel(): ViewModel() {
    //val coreNotificationViewModel = coreNotificationViewModel

    var notificationFilterList: List<NotificationFitler> = listOf(
        NotificationFitler(
            title = "All Notifications",
            value = "",
            selected = true
        ),
        NotificationFitler(
            title = "Show unread messages",
            value = "unread",
            selected = false
        ),
        NotificationFitler(
            title = "Show important messages",
            value = "important",
            selected = false
        )
    )

    /*init {
        viewModelScope.launch(Dispatchers.IO) {
            coreViewModel.studyModel.collect{
                withContext(Dispatchers.Main) {
                    model.value = it
                }
            }
        }
    }*/

    fun processFilter(filterTitle: String) {
        //coreViewModel.setFilter(filter)

        notificationFilterList.forEach{
            println("----------------------")
            println(filterTitle)
            println(it.title)
            println(it.title == filterTitle)
            it.selected = (it.title == filterTitle)
        }
    }
}