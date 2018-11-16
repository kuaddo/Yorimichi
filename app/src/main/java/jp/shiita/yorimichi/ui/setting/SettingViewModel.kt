package jp.shiita.yorimichi.ui.setting

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import javax.inject.Inject

class SettingViewModel @Inject constructor() : ViewModel() {
    val originalCategory1 = MutableLiveData<String>()
    val originalCategory2 = MutableLiveData<String>()
    val originalCategory3 = MutableLiveData<String>()

    val showLicensesEvent: LiveData<Unit>
        get() = _showLicensesEvent

    private val _showLicensesEvent = SingleUnitLiveEvent()

    fun showLicenses() = _showLicensesEvent.call()

    fun setOriginalCategories() {
        originalCategory1.postValue(UserInfo.originalCategory1)
        originalCategory2.postValue(UserInfo.originalCategory2)
        originalCategory3.postValue(UserInfo.originalCategory3)
    }

    fun saveOriginalCategories() {
        UserInfo.originalCategory1 = originalCategory1.value ?: ""
        UserInfo.originalCategory2 = originalCategory2.value ?: ""
        UserInfo.originalCategory3 = originalCategory3.value ?: ""
    }
}