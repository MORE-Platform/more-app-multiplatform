package io.redlink.more.app.android.activities.observations.pushButton

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.observations.extensions.pushButtonObservation.PushButtonCoreViewModel
import kotlinx.coroutines.*
import kotlin.random.Random

class PushButtonViewModel : ViewModel() {
    private val coreViewModel: PushButtonCoreViewModel = PushButtonCoreViewModel(
        MoreApplication.shared!!.observationFactory)

    val observationTitle = mutableStateOf("")
    val buttonText = mutableStateOf("")

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    init {
        scope.launch {
            coreViewModel.pushButtonModel.collect { model ->
                model?.let {
                    withContext(Dispatchers.Main) {
                        observationTitle.value = it.observationTitle
                        buttonText.value = it.buttonText
                    }
                }
            }
        }
    }

    fun viewDidAppear() {
        coreViewModel.viewDidAppear()
    }

    fun viewDidDisappear() {
        coreViewModel.viewDidDisappear()
    }

    fun setScheduleId(scheduleId: String) {
        coreViewModel.setScheduleId(scheduleId)
    }

    fun finish() {
        coreViewModel.finish()
    }

    fun click() {
        coreViewModel.click(Random.nextInt(0, 100))
    }

}