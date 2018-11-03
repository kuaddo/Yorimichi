package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
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
    val finishAppMessage: LiveData<Int> get() = _finishAppMessage
    val points: LiveData<Int> get() = _points
    var homeAsUpType: HomeAsUpType = HomeAsUpType.POP_BACK_STACK
        private set

    private val _titleEvent = SingleLiveEvent<Int>()
    private val _homeAsUpIndicator = SingleLiveEvent<Int>()
    private val _displayHomeAsUpEnabled = SingleLiveEvent<Boolean>()
    private val _drawerLock = SingleLiveEvent<Boolean>()
    private val _finishAppMessage = SingleLiveEvent<Int>()
    private val _points = MutableLiveData<Int>().apply { value = UserInfo.points }

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

    fun finishAppLocationPermissionDenied() = _finishAppMessage.postValue(R.string.dialog_location_permission_denied_message)

    fun setPoints(points: Int) {
        _points.postValue(points)
    }

    fun createOrUpdateUser() {
        if (UserInfo.userId.isEmpty()) {
            repository.createUser()
                    .subscribeOn(scheduler.io())
                    .subscribeBy(
                            onSuccess = { UserInfo.userId = it },
                            onError = { _finishAppMessage.postValue(R.string.dialog_location_permission_denied_message) }
                    )
                    .addTo(disposables)
        }
        else {
            repository.getUser(UserInfo.userId)
                    .subscribeOn(scheduler.io())
                    .subscribeBy(
                            onSuccess = {
                                UserInfo.points = it
                                setPoints(it)
                            },
                            onError = {}
                    )
                    .addTo(disposables)
        }
    }

    enum class HomeAsUpType { OPEN_DRAWER, POP_BACK_STACK }
}