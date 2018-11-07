package jp.shiita.yorimichi.ui.remind

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import javax.inject.Inject

class RemindViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val reachedVisible: LiveData<Boolean> get() = _reachedVisible
    val gotoVisible: LiveData<Boolean> get() = _gotoVisible
    val placeVisible: LiveData<Boolean> get() = _placeVisible
    val timeVisible: LiveData<Boolean> get() = _timeVisible
    val finishVisible: LiveData<Boolean> get() = _finishVisible
    val finishWithNeed: LiveData<Boolean> get() = _finishWithNeed

    val finishEvent: LiveData<Unit> get() = _finishEvent

    private val _reachedVisible = MutableLiveData<Boolean>().apply { value = true }
    private val _gotoVisible = MutableLiveData<Boolean>()
    private val _placeVisible = MutableLiveData<Boolean>()
    private val _timeVisible = MutableLiveData<Boolean>()
    private val _finishVisible = MutableLiveData<Boolean>()
    private val _finishWithNeed = MutableLiveData<Boolean>()

    private val _finishEvent = SingleUnitLiveEvent()

    fun need() {
        _reachedVisible.value = false
        _gotoVisible.value = true
    }

    fun noNeed() {
        _reachedVisible.value = false
        _finishVisible.value = true
        _finishWithNeed.value = false
    }

    fun goBack() {
        _gotoVisible.value = false
        _timeVisible.value = true
    }

    fun goOther() {
        _gotoVisible.value = false
        _placeVisible.value = true
    }

    fun gotoPlace() {
        _placeVisible.value = false
        _timeVisible.value = true
    }

    fun timeSelected() {
        _timeVisible.value = false
        _finishVisible.value = true
        _finishWithNeed.value = true
        // TODO: set notification
    }

    fun finish() {
        _finishEvent.call()
    }
}