package jp.shiita.yorimichi.ui.setting

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import javax.inject.Inject

class SettingViewModel @Inject constructor() : ViewModel() {
    val showLicensesEvent: LiveData<Unit>
        get() = _showLicensesEvent

    private val _showLicensesEvent = SingleUnitLiveEvent()

    fun showLicenses() = _showLicensesEvent.call()
}