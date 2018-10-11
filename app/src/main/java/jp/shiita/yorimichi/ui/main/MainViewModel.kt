package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.util.SingleLiveEvent
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {
    val titleEvent: LiveData<Int> get() = _titleEvent
    val homeAsUpIndicator: LiveData<Int> get() = _homeAsUpIndicator
    val displayHomeAsUpEnabled: LiveData<Boolean> get() = _displayHomeAsUpEnabled
    var homeAsUpType: HomeAsUpType = HomeAsUpType.POP_BACK_STACK
        private set

    private val _titleEvent = SingleLiveEvent<Int>()
    private val _homeAsUpIndicator = SingleLiveEvent<Int>()
    private val _displayHomeAsUpEnabled = SingleLiveEvent<Boolean>()

    fun setupActionBar(@StringRes titleRes: Int = R.string.app_name,
                       @DrawableRes indicatorRes: Int = R.drawable.ic_back,
                       enabled: Boolean = true,
                       type: HomeAsUpType = HomeAsUpType.POP_BACK_STACK) {
        _titleEvent.postValue(titleRes)
        _homeAsUpIndicator.postValue(indicatorRes)
        _displayHomeAsUpEnabled.postValue(enabled)
        homeAsUpType = type
    }

    enum class HomeAsUpType { OPEN_DRAWER, POP_BACK_STACK }
}