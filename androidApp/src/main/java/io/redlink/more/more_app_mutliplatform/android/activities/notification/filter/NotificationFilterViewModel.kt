import androidx.lifecycle.ViewModel

data class NotificationFitler (
    val title: String,
    val value: String,
    var selected: Boolean
)

class NotificationFilterViewModel(): ViewModel() {
}