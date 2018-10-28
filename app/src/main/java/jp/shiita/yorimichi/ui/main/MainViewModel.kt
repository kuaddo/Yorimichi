package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.SingleLiveEvent
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.scheduler.BaseSchedulerProvider
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val repository: YorimichiRepository,
        private val scheduler: BaseSchedulerProvider
) : ViewModel() {
    val titleEvent: LiveData<Int> get() = _titleEvent
    val homeAsUpIndicator: LiveData<Int> get() = _homeAsUpIndicator
    val displayHomeAsUpEnabled: LiveData<Boolean> get() = _displayHomeAsUpEnabled
    val drawerLock: LiveData<Boolean> get() = _drawerLock
    val finishApp: LiveData<Unit> get() = _finishAppEvent
    var homeAsUpType: HomeAsUpType = HomeAsUpType.POP_BACK_STACK
        private set

    private val _titleEvent = SingleLiveEvent<Int>()
    private val _homeAsUpIndicator = SingleLiveEvent<Int>()
    private val _displayHomeAsUpEnabled = SingleLiveEvent<Boolean>()
    private val _drawerLock = SingleLiveEvent<Boolean>()
    private val _finishAppEvent = SingleUnitLiveEvent()

    private val disposables = CompositeDisposable()

    override fun onCleared() = disposables.clear()

    fun setupActionBar(@StringRes titleRes: Int = R.string.app_name,
                       @DrawableRes indicatorRes: Int = R.drawable.ic_back,
                       enabled: Boolean = true,
                       type: HomeAsUpType = HomeAsUpType.POP_BACK_STACK) {
        _titleEvent.postValue(titleRes)
        _homeAsUpIndicator.postValue(indicatorRes)
        _displayHomeAsUpEnabled.postValue(enabled)
        homeAsUpType = type
    }

    fun setDrawerLock(locked: Boolean) = _drawerLock.postValue(locked)

    fun finishApp() = _finishAppEvent.call()

    fun createUser() {
        if (UserInfo.userId.isEmpty()) repository.createUser()
                .subscribeOn(scheduler.io())
                .observeOn(scheduler.ui())
                .subscribeBy(
                        onSuccess = { UserInfo.userId = it },
                        onError = {
                            // TODO: 終了ダイアログ、メッセージを指定できるように
                        }
                )
                .addTo(disposables)
    }

    enum class HomeAsUpType { OPEN_DRAWER, POP_BACK_STACK }
}