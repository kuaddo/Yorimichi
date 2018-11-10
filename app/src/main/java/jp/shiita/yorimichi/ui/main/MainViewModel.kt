package jp.shiita.yorimichi.ui.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jp.shiita.yorimichi.R
import jp.shiita.yorimichi.data.User
import jp.shiita.yorimichi.data.UserInfo
import jp.shiita.yorimichi.data.api.YorimichiRepository
import jp.shiita.yorimichi.live.LiveEvent
import jp.shiita.yorimichi.live.SingleLiveEvent
import jp.shiita.yorimichi.live.SingleUnitLiveEvent
import jp.shiita.yorimichi.live.UnitLiveEvent
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
    val updatePointEvent: LiveData<Unit> get() = _updatePointEvent
    val updateIconEvent: LiveData<Unit> get() = _updateIconEvent
    val searchEvent: LiveData<Pair<List<String>, Int>> get() = _searchEvent
    val directionsEvent: LiveData<LatLng> get() = _directionsEvent
    var homeAsUpType: HomeAsUpType = HomeAsUpType.POP_BACK_STACK
        private set

    private val _titleEvent = SingleLiveEvent<Int>()
    private val _homeAsUpIndicator = SingleLiveEvent<Int>()
    private val _displayHomeAsUpEnabled = SingleLiveEvent<Boolean>()
    private val _drawerLock = SingleLiveEvent<Boolean>()
    private val _finishAppMessage = SingleLiveEvent<Int>()
    private val _updatePointEvent = SingleUnitLiveEvent()
    private val _updateIconEvent = UnitLiveEvent()
    private val _searchEvent = LiveEvent<Pair<List<String>, Int>>()
    private val _directionsEvent = LiveEvent<LatLng>()

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

    fun search(categories: List<String>, radius: Int) = _searchEvent.postValue(categories to radius)

    fun callDirectionsEvent(latLng: LatLng) = _directionsEvent.postValue(latLng)

    fun updatePoints() = _updatePointEvent.call()

    fun updateIcon() = _updateIconEvent.call()

    fun createOrUpdateUser() {
        if (UserInfo.userId.isEmpty()) {
            repository.createUser()
                    .subscribeOn(scheduler.io())
                    .subscribeBy(
                            onSuccess = { reflectUserInfo(it) },
                            onError = { _finishAppMessage.postValue(R.string.dialog_location_permission_denied_message) }
                    )
                    .addTo(disposables)
        }
        else {
            repository.getUser(UserInfo.userId)
                    .subscribeOn(scheduler.io())
                    .subscribeBy(
                            onSuccess = { reflectUserInfo(it) },
                            onError = {}
                    )
                    .addTo(disposables)
        }
    }

    private fun reflectUserInfo(user: User) {
        UserInfo.userId = user.uuid
        UserInfo.points = user.points
        updatePoints()

        repository.getIcon(user.iconId)
                .subscribeOn(scheduler.io())
                .subscribeBy(
                        onSuccess = {
                            UserInfo.iconBucket = it.first
                            UserInfo.iconFileName = it.second
                            updateIcon()
                        },
                        onError = {}
                )
                .addTo(disposables)
    }

    enum class HomeAsUpType { OPEN_DRAWER, POP_BACK_STACK }
}